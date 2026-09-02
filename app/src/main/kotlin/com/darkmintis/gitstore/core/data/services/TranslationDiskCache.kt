package com.darkmintis.gitstore.core.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/** ponytail: 500 entries ~ few MB; upgrade path = Room table with repo id + lang keys */
class TranslationDiskCache(
    private val file: File,
    private val maxEntries: Int = 500,
) {
    private val map = object : LinkedHashMap<String, String>(maxEntries + 1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>): Boolean =
            size > maxEntries
    }

    suspend fun load() = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext
        runCatching {
            val json = JSONObject(file.readText())
            val entries = json.optJSONObject("entries") ?: return@runCatching
            map.clear()
            for (key in entries.keys()) {
                map[key] = entries.getString(key)
            }
        }
    }

    fun get(key: String): String? = map[key]

    suspend fun put(key: String, value: String) = withContext(Dispatchers.IO) {
        map[key] = value
        runCatching {
            val entries = JSONObject()
            for ((k, v) in map) {
                entries.put(k, v)
            }
            file.parentFile?.mkdirs()
            file.writeText(JSONObject().put("entries", entries).toString())
        }
    }
}
