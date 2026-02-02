package com.darkmintis.gitstore.feature.details.presentation.utils

import com.darkmintis.gitstore.core.domain.model.Architecture

fun extractArchitectureFromName(name: String): String? {
    val lower = name.lowercase()
    return when {
        lower.contains("x86_64") || lower.contains("amd64") || lower.contains("x64") -> "x86_64"
        lower.contains("aarch64") || lower.contains("arm64") -> "aarch64"
        lower.contains("i386") || lower.contains("i686") -> "i386"
        lower.contains("armv7") || lower.contains("arm") -> "arm"
        else -> null
    }
}

fun isExactArchitectureMatch(assetName: String, systemArch: Architecture): Boolean {
    val name = assetName.lowercase()
    return when (systemArch) {
        Architecture.X86_64 -> name.contains("x86_64") || name.contains("amd64") || name.contains("x64")
        Architecture.AARCH64 -> name.contains("aarch64") || name.contains("arm64")
        Architecture.X86 -> name.contains("i386") || name.contains("i686")
        Architecture.ARM -> name.contains("armv7") || name.contains("arm")
        Architecture.UNKNOWN -> false
    }
}

