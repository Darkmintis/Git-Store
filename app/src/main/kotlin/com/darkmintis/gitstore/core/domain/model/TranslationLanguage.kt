package com.darkmintis.gitstore.core.domain.model

enum class TranslationLanguage(val code: String, val displayName: String, val nativeName: String) {
    SYSTEM("system", "System Default", "Langue du système"),
    FRENCH("fr", "French", "Français"),
    ENGLISH("en", "English", "English"),
    SPANISH("es", "Spanish", "Español"),
    GERMAN("de", "German", "Deutsch"),
    ITALIAN("it", "Italian", "Italiano"),
    PORTUGUESE("pt", "Portuguese", "Português"),
    RUSSIAN("ru", "Russian", "Русский"),
    CHINESE_SIMPLIFIED("zh-CN", "Chinese (Simplified)", "简体中文"),
    CHINESE_TRADITIONAL("zh-TW", "Chinese (Traditional)", "繁體中文"),
    JAPANESE("ja", "Japanese", "日本語"),
    KOREAN("ko", "Korean", "한국어"),
    ARABIC("ar", "Arabic", "العربية"),
    TURKISH("tr", "Turkish", "Türkçe"),
    DUTCH("nl", "Dutch", "Nederlands"),
    POLISH("pl", "Polish", "Polski");

    companion object {
        fun fromCode(code: String?): TranslationLanguage {
            if (code.isNullOrBlank()) return FRENCH
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FRENCH
        }
    }
}
