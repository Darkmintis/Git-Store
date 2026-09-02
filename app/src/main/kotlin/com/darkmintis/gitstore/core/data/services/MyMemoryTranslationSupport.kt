package com.darkmintis.gitstore.core.data.services

import java.security.MessageDigest

internal object MyMemoryTranslationSupport {
    const val MAX_QUERY_CHARS = 450

    fun cacheKey(source: String, target: String, text: String): String {
        val raw = "$source|$target|$text"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun chunkForQuery(text: String, maxChars: Int = MAX_QUERY_CHARS): List<String> {
        if (text.length <= maxChars) return listOf(text)

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            var end = minOf(start + maxChars, text.length)
            if (end < text.length) {
                val slice = text.substring(start, end)
                val breakAt = slice.lastIndexOfAny(charArrayOf(' ', '\n', '\t', '.', ',', ';', '。', '，'))
                if (breakAt > maxChars / 3) {
                    end = start + breakAt + 1
                }
            }
            chunks.add(text.substring(start, end))
            start = end
        }
        return chunks.filter { it.isNotEmpty() }
    }

    fun isUsableTranslation(candidate: String?, original: String): Boolean {
        if (candidate.isNullOrBlank()) return false
        if (candidate.equals(original, ignoreCase = true)) return false

        val upper = candidate.uppercase()
        if (upper.contains("MYMEMORY")) return false
        if (upper.contains("PLEASE SELECT")) return false
        if (upper.contains("QUERY LENGTH")) return false
        if (upper.contains("MAX ALLOWED")) return false
        if (upper.contains("LIMIT EXCEEDED")) return false
        if (upper.contains("QUOTA")) return false
        if (upper.contains("WARNING:")) return false
        if (upper.contains("INVALID")) return false
        if (upper.startsWith("ERROR:")) return false
        if (upper.contains("VISIT HTTPS://")) return false

        return true
    }

    /** Groups prose lines into blocks; code fences, blanks, and images stay verbatim. */
    fun splitMarkdownSegments(markdown: String): List<MarkdownSegment> {
        val segments = mutableListOf<MarkdownSegment>()
        var inCodeBlock = false
        val block = StringBuilder()

        fun flushBlock() {
            if (block.isEmpty()) return
            segments.add(MarkdownSegment(block.toString(), translatable = true))
            block.clear()
        }

        for (line in markdown.lines()) {
            if (line.trimStart().startsWith("```")) {
                flushBlock()
                inCodeBlock = !inCodeBlock
                segments.add(MarkdownSegment("$line\n", translatable = false))
                continue
            }
            if (inCodeBlock || line.isBlank() ||
                line.trim().startsWith("<img") || line.trim().startsWith("![")
            ) {
                flushBlock()
                segments.add(MarkdownSegment("$line\n", translatable = false))
                continue
            }
            block.appendLine(line)
        }
        flushBlock()
        return segments
    }
}

internal data class MarkdownSegment(val text: String, val translatable: Boolean)
