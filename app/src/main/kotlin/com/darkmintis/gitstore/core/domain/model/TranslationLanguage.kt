package com.darkmintis.gitstore.core.domain.model

enum class TranslationLanguage(val code: String, val displayName: String, val nativeName: String) {
    SYSTEM("system", "System default", "System default"),
    ENGLISH("en", "English", "English"),
    ARABIC("ar", "Arabic", "العربية"),
    CHINESE_SIMPLIFIED("zh-CN", "Chinese (Simplified)", "简体中文"),
    CHINESE_TRADITIONAL("zh-TW", "Chinese (Traditional)", "繁體中文"),
    DUTCH("nl", "Dutch", "Nederlands"),
    FRENCH("fr", "French", "Français"),
    GERMAN("de", "German", "Deutsch"),
    ITALIAN("it", "Italian", "Italiano"),
    JAPANESE("ja", "Japanese", "日本語"),
    KOREAN("ko", "Korean", "한국어"),
    POLISH("pl", "Polish", "Polski"),
    PORTUGUESE("pt", "Portuguese", "Português"),
    RUSSIAN("ru", "Russian", "Русский"),
    SPANISH("es", "Spanish", "Español"),
    TURKISH("tr", "Turkish", "Türkçe");

    companion object {
        /** Languages with a complete app strings.xml (plus system + English). */
        val pickerOptions: List<TranslationLanguage> = listOf(
            SYSTEM,
            ENGLISH,
            FRENCH,
            GERMAN,
            SPANISH,
            JAPANESE,
            RUSSIAN,
            CHINESE_SIMPLIFIED,
        )

        fun fromCode(code: String?): TranslationLanguage {
            if (code.isNullOrBlank()) return SYSTEM
            val match = entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: return SYSTEM
            return if (match in pickerOptions) match else SYSTEM
        }
    }
}
