package com.darkmintis.gitstore.core.presentation.model

enum class FontTheme(val displayName: String) {
    SYSTEM("System"),
    CUSTOM("JetBrains Mono + Inter");

    companion object {
        fun fromName(name: String?): FontTheme {
            return entries.find { it.name == name } ?: CUSTOM
        }
    }
}

