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
import org.json.JSONObject
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

class MyMemoryTranslationService(
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
                val translated = translateViaMyMemory(text, lang) ?: text
                cache[cacheKey] = translated
                translated
            } catch (e: Exception) {
                Logger.e(e) { "Translation failed for: $text" }
                text
            }
        }
    }

    // ponytail: MyMemory free tier ~5k chars/day; upgrade path = paid MyMemory or self-hosted LibreTranslate
    private fun translateViaMyMemory(text: String, targetLang: String): String? {
        return try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString =
                "https://api.mymemory.translated.net/get?q=$encodedText&langpair=autodetect|$targetLang"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
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
