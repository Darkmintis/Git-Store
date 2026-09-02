package com.darkmintis.gitstore.core.data.utils

object ContentLanguageDetector {

    fun detect(text: String): String? {
        val sample = text.take(1000)
        if (sample.isBlank()) return null

        val sampleLower = sample.lowercase()

        val chineseChars = sample.count { it in '\u4e00'..'\u9fff' }
        val japaneseHiragana = sample.count { it in '\u3040'..'\u309f' }
        val japaneseKatakana = sample.count { it in '\u30a0'..'\u30ff' }
        val koreanChars = sample.count { it in '\uac00'..'\ud7af' }
        val arabicChars = sample.count { it in '\u0600'..'\u06ff' }
        val cyrillicChars = sample.count { it in 'а'..'я' || it in 'А'..'Я' || it == 'ё' || it == 'Ё' }

        val totalChars = sample.length
        val threshold = 0.15

        return when {
            chineseChars > totalChars * threshold -> "zh"
            (japaneseHiragana + japaneseKatakana) > totalChars * threshold -> "ja"
            koreanChars > totalChars * threshold -> "ko"
            arabicChars > totalChars * threshold -> "ar"
            cyrillicChars > totalChars * threshold -> "ru"
            looksLikeEnglish(sampleLower) -> "en"
            else -> null
        }
    }

    private fun looksLikeEnglish(sampleLower: String): Boolean {
        val englishIndicators = listOf(
            "\\bthe\\b", "\\band\\b", "\\bfor\\b", "\\bwith\\b",
            "\\bthis\\b", "\\bthat\\b", "\\bfrom\\b", "\\bare\\b",
            "\\bwas\\b", "\\bhave\\b", "\\bhas\\b", "\\bwill\\b",
            "\\byou\\b", "\\bcan\\b", "\\buse\\b", "\\binstall\\b"
        )
        val matchCount = englishIndicators.count { pattern ->
            Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(sampleLower)
        }
        return matchCount >= 4
    }

    fun normalizeLanguageCode(code: String): String {
        val lower = code.lowercase()
        return when {
            lower.startsWith("zh") -> "zh"
            else -> lower.substringBefore('-').substringBefore('_')
        }
    }
}
