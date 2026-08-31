package com.darkmintis.gitstore.core.data.services

import co.touchlab.kermit.Logger
import com.darkmintis.gitstore.core.domain.model.TranslationLanguage
import com.darkmintis.gitstore.core.domain.repository.TranslationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

interface TranslationService {
    val targetLanguage: StateFlow<TranslationLanguage>
    val isAutoTranslateEnabled: StateFlow<Boolean>
    fun getEffectiveLanguageCode(): String
    suspend fun translate(text: String, targetLang: String? = null): String
    suspend fun translateMarkdown(markdown: String, targetLang: String? = null): String
    fun containsNonLatin(text: String): Boolean
    fun isChinese(text: String): Boolean
}

class GoogleTranslationService(
    private val translationRepository: TranslationRepository,
    private val localizationManager: LocalizationManager,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : TranslationService {

    private val cache = ConcurrentHashMap<String, String>()

    override val targetLanguage: StateFlow<TranslationLanguage> = translationRepository
        .getTargetLanguage()
        .stateIn(scope, SharingStarted.Eagerly, TranslationLanguage.FRENCH)

    override val isAutoTranslateEnabled: StateFlow<Boolean> = translationRepository
        .isAutoTranslateEnabled()
        .stateIn(scope, SharingStarted.Eagerly, true)

    override fun getEffectiveLanguageCode(): String {
        val selected = targetLanguage.value
        return if (selected == TranslationLanguage.SYSTEM) {
            localizationManager.getPrimaryLanguageCode().ifBlank { "fr" }
        } else {
            selected.code
        }
    }

    override fun containsNonLatin(text: String): Boolean {
        return text.any { 
            it in '\u4e00'..'\u9fff' || // Chinese
            it in '\u3040'..'\u30ff' || // Japanese
            it in '\uac00'..'\ud7af' || // Korean
            it in '\u0400'..'\u04ff' || // Cyrillic
            it in '\u0600'..'\u06ff'    // Arabic
        }
    }

    override fun isChinese(text: String): Boolean {
        return text.any { it in '\u4e00'..'\u9fff' }
    }

    override suspend fun translate(text: String, targetLang: String?): String {
        if (text.isBlank()) return text

        val lang = targetLang ?: getEffectiveLanguageCode()
        val cacheKey = "$lang:$text"
        cache[cacheKey]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val translated = translateViaGoogleClients(text, lang)
                    ?: translateViaMyMemory(text, lang)
                    ?: text

                cache[cacheKey] = translated
                translated
            } catch (e: Exception) {
                Logger.e(e) { "Translation failed for: $text" }
                text
            }
        }
    }

    private fun translateViaGoogleClients(text: String, targetLang: String): String? {
        return try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=auto&tl=$targetLang&q=$encodedText"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseText)
                if (jsonArray.length() > 0) {
                    val rawResult = when (val item = jsonArray.get(0)) {
                        is JSONArray -> {
                            val sb = StringBuilder()
                            for (i in 0 until item.length()) {
                                val piece = item.opt(i)
                                if (piece is JSONArray && piece.length() > 0) {
                                    sb.append(piece.optString(0))
                                } else if (piece is String) {
                                    sb.append(piece)
                                }
                            }
                            sb.toString().ifBlank { item.toString() }
                        }
                        else -> item.toString()
                    }
                    val cleaned = cleanTranslatedText(rawResult)
                    if (cleaned.isNotBlank()) {
                        return cleaned
                    }
                }
            }
            null
        } catch (e: Exception) {
            Logger.w { "Google clients translate failed: ${e.message}" }
            null
        }
    }

    private fun cleanTranslatedText(raw: String): String {
        var text = raw.trim()
        while ((text.startsWith("[") && text.endsWith("]")) || (text.startsWith("\"") && text.endsWith("\""))) {
            if (text.startsWith("[") && text.endsWith("]")) {
                text = text.removeSurrounding("[", "]").trim()
            }
            if (text.startsWith("\"") && text.endsWith("\"")) {
                text = text.removeSurrounding("\"", "\"").trim()
            }
        }
        return text
    }

    private fun translateViaMyMemory(text: String, targetLang: String): String? {
        return try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://api.mymemory.translated.net/get?q=$encodedText&langpair=autodetect|$targetLang"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(responseText)
                val responseData = json.optJSONObject("responseData")
                val translatedText = responseData?.optString("translatedText")
                if (!translatedText.isNullOrBlank() && !translatedText.contains("MYMEMORY WARNING")) {
                    return translatedText
                }
            }
            null
        } catch (e: Exception) {
            Logger.w { "MyMemory translate failed: ${e.message}" }
            null
        }
    }

    override suspend fun translateMarkdown(markdown: String, targetLang: String?): String {
        if (markdown.isBlank()) return markdown

        val lang = targetLang ?: getEffectiveLanguageCode()
        val cacheKey = "md:$lang:${markdown.hashCode()}"
        cache[cacheKey]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val lines = markdown.lines()
                val resultLines = mutableListOf<String>()
                var inCodeBlock = false

                for (line in lines) {
                    if (line.trimStart().startsWith("```")) {
                        inCodeBlock = !inCodeBlock
                        resultLines.add(line)
                        continue
                    }

                    if (inCodeBlock || line.isBlank() || line.trim().startsWith("<img") || line.trim().startsWith("![")) {
                        resultLines.add(line)
                        continue
                    }

                    if (line.startsWith("#") || line.startsWith("- ") || line.startsWith("* ") || line.length > 2) {
                        val translatedLine = translate(line, lang)
                        resultLines.add(translatedLine)
                    } else {
                        resultLines.add(line)
                    }
                }

                val translatedMarkdown = resultLines.joinToString("\n")
                cache[cacheKey] = translatedMarkdown
                translatedMarkdown
            } catch (e: Exception) {
                Logger.e(e) { "Markdown translation failed" }
                markdown
            }
        }
    }
}
