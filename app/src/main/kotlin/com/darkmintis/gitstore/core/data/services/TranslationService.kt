package com.darkmintis.gitstore.core.data.services

import co.touchlab.kermit.Logger
import com.darkmintis.gitstore.core.data.utils.ContentLanguageDetector
import com.darkmintis.gitstore.core.domain.model.TranslationLanguage
import com.darkmintis.gitstore.core.domain.repository.TranslationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

interface TranslationService {
    val targetLanguage: StateFlow<TranslationLanguage>
    val isAutoTranslateEnabled: StateFlow<Boolean>
    fun getEffectiveLanguageCode(): String
    fun needsTranslation(text: String): Boolean
    suspend fun translate(text: String, targetLang: String? = null): String
    suspend fun translateMarkdown(markdown: String, targetLang: String? = null): String
}

class MyMemoryTranslationService(
    private val translationRepository: TranslationRepository,
    private val localizationManager: LocalizationManager,
    private val diskCache: TranslationDiskCache? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : TranslationService {

    private val memoryCache = ConcurrentHashMap<String, String>()
    private val failedKeys = ConcurrentHashMap.newKeySet<String>()
    private val apiMutex = Mutex()
    private var diskCacheReady = false

    init {
        if (diskCache != null) {
            scope.launch { loadDiskCache() }
        }
    }

    override val targetLanguage: StateFlow<TranslationLanguage> = translationRepository
        .getTargetLanguage()
        .stateIn(scope, SharingStarted.Eagerly, TranslationLanguage.SYSTEM)

    override val isAutoTranslateEnabled: StateFlow<Boolean> = translationRepository
        .isAutoTranslateEnabled()
        .stateIn(scope, SharingStarted.Eagerly, false)

    override fun getEffectiveLanguageCode(): String {
        val selected = targetLanguage.value
        return if (selected == TranslationLanguage.SYSTEM) {
            localizationManager.getPrimaryLanguageCode().ifBlank { "en" }
        } else {
            selected.code
        }
    }

    override fun needsTranslation(text: String): Boolean {
        if (text.isBlank()) return false

        val target = ContentLanguageDetector.normalizeLanguageCode(getEffectiveLanguageCode())
        val detected = ContentLanguageDetector.detect(text) ?: return false

        return ContentLanguageDetector.normalizeLanguageCode(detected) != target
    }

    override suspend fun translate(text: String, targetLang: String?): String =
        translateInternal(text, targetLang, persist = true)

    private suspend fun translateInternal(
        text: String,
        targetLang: String?,
        persist: Boolean,
    ): String {
        if (text.isBlank() || !needsTranslation(text)) return text

        val target = targetLang ?: getEffectiveLanguageCode()
        val source = ContentLanguageDetector.detect(text) ?: return text
        val cacheKey = MyMemoryTranslationSupport.cacheKey(source, target, text)

        memoryCache[cacheKey]?.let { return it }
        loadDiskCache()
        diskCache?.get(cacheKey)?.let { cached ->
            memoryCache[cacheKey] = cached
            return cached
        }
        if (failedKeys.contains(cacheKey)) return text

        return withContext(Dispatchers.IO) {
            try {
                val chunks = MyMemoryTranslationSupport.chunkForQuery(text)
                val translated = chunks.map { chunk ->
                    translateViaMyMemory(chunk, source, target) ?: chunk
                }.joinToString("")

                if (translated == text || !MyMemoryTranslationSupport.isUsableTranslation(translated, text)) {
                    failedKeys.add(cacheKey)
                    text
                } else {
                    memoryCache[cacheKey] = translated
                    if (persist) {
                        diskCache?.put(cacheKey, translated)
                    }
                    translated
                }
            } catch (e: Exception) {
                Logger.e(e) { "Translation failed" }
                failedKeys.add(cacheKey)
                text
            }
        }
    }

    // ponytail: MyMemory free tier ~5k chars/day; upgrade path = paid MyMemory or self-hosted LibreTranslate
    private suspend fun translateViaMyMemory(
        text: String,
        sourceLang: String,
        targetLang: String,
    ): String? = apiMutex.withLock {
        val source = toMyMemoryCode(sourceLang)
        val target = toMyMemoryCode(targetLang)
        if (source == target) return null
        if (text.length > MyMemoryTranslationSupport.MAX_QUERY_CHARS) return null

        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString =
                "https://api.mymemory.translated.net/get?q=$encodedText&langpair=$source|$target"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode != 200) return null

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            val translatedText = json.optJSONObject("responseData")?.optString("translatedText")
            if (MyMemoryTranslationSupport.isUsableTranslation(translatedText, text)) translatedText else null
        } catch (e: Exception) {
            Logger.w { "MyMemory translate failed: ${e.message}" }
            null
        }
    }

    private fun toMyMemoryCode(code: String): String {
        val normalized = ContentLanguageDetector.normalizeLanguageCode(code)
        return when {
            normalized == "zh" && code.contains("tw", ignoreCase = true) -> "zh-TW"
            normalized == "zh" -> "zh-CN"
            else -> normalized
        }
    }

    override suspend fun translateMarkdown(markdown: String, targetLang: String?): String {
        if (markdown.isBlank() || !needsTranslation(markdown)) return markdown

        val lang = targetLang ?: getEffectiveLanguageCode()
        val cacheKey = MyMemoryTranslationSupport.cacheKey("md", lang, markdown)

        memoryCache[cacheKey]?.let { return it }
        loadDiskCache()
        diskCache?.get(cacheKey)?.let { cached ->
            memoryCache[cacheKey] = cached
            return cached
        }
        if (failedKeys.contains(cacheKey)) return markdown

        return withContext(Dispatchers.IO) {
            try {
                val segments = MyMemoryTranslationSupport.splitMarkdownSegments(markdown)
                val translatedMarkdown = buildString {
                    for (segment in segments) {
                        if (segment.translatable) {
                            append(translateInternal(segment.text.trimEnd(), lang, persist = false))
                            if (segment.text.endsWith('\n')) append('\n')
                        } else {
                            append(segment.text)
                        }
                    }
                }

                if (translatedMarkdown != markdown &&
                    MyMemoryTranslationSupport.isUsableTranslation(translatedMarkdown, markdown)
                ) {
                    memoryCache[cacheKey] = translatedMarkdown
                    diskCache?.put(cacheKey, translatedMarkdown)
                    translatedMarkdown
                } else {
                    failedKeys.add(cacheKey)
                    markdown
                }
            } catch (e: Exception) {
                Logger.e(e) { "Markdown translation failed" }
                failedKeys.add(cacheKey)
                markdown
            }
        }
    }

    private suspend fun loadDiskCache() {
        if (diskCache == null || diskCacheReady) return
        diskCache.load()
        diskCacheReady = true
    }
}
