package com.darkmintis.gitstore.feature.details.data.utils

import com.darkmintis.gitstore.core.data.services.LocalizationManager
import com.darkmintis.gitstore.core.data.utils.ContentLanguageDetector
import com.darkmintis.gitstore.feature.details.data.model.ReadmeAttempt

class ReadmeLocalizationHelper(
    private val localizationManager: LocalizationManager
) {

    private val searchPaths = listOf(
        ".github",
        "",
        "docs",
        "doc"
    )

    fun generateReadmeAttempts(): List<ReadmeAttempt> {
        val attempts = mutableListOf<ReadmeAttempt>()
        val currentLang = localizationManager.getCurrentLanguageCode().lowercase()
        val primaryLang = localizationManager.getPrimaryLanguageCode().lowercase()

        var globalPriority = 0

        for ((pathIndex, searchPath) in searchPaths.withIndex()) {
            val pathPrefix = if (searchPath.isEmpty()) "" else "$searchPath/"

            var localPriority = 0

            if (currentLang.contains("-")) {
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README.${currentLang}.md",
                    filename = "README.${currentLang}.md",
                    priority = globalPriority + localPriority++
                ))
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README.${currentLang.replace("-", "_")}.md",
                    filename = "README.${currentLang.replace("-", "_")}.md",
                    priority = globalPriority + localPriority++
                ))
            }

            attempts.add(ReadmeAttempt(
                path = "${pathPrefix}README.${primaryLang}.md",
                filename = "README.${primaryLang}.md",
                priority = globalPriority + localPriority++
            ))

            if (currentLang.contains("-")) {
                val parts = currentLang.split("-")
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README.${parts[0].uppercase()}.md",
                    filename = "README.${parts[0].uppercase()}.md",
                    priority = globalPriority + localPriority++
                ))
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README-${parts[0].uppercase()}.md",
                    filename = "README-${parts[0].uppercase()}.md",
                    priority = globalPriority + localPriority++
                ))
            } else {
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README.${primaryLang.uppercase()}.md",
                    filename = "README.${primaryLang.uppercase()}.md",
                    priority = globalPriority + localPriority++
                ))
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README-${primaryLang.uppercase()}.md",
                    filename = "README-${primaryLang.uppercase()}.md",
                    priority = globalPriority + localPriority++
                ))
            }

            attempts.add(ReadmeAttempt(
                path = "${pathPrefix}README_${primaryLang}.md",
                filename = "README_${primaryLang}.md",
                priority = globalPriority + localPriority++
            ))
            attempts.add(ReadmeAttempt(
                path = "${pathPrefix}readme.${primaryLang}.md",
                filename = "readme.${primaryLang}.md",
                priority = globalPriority + localPriority++
            ))

            attempts.add(ReadmeAttempt(
                path = "${pathPrefix}README.md",
                filename = "README.md",
                priority = globalPriority + localPriority++
            ))

            if (primaryLang != "en") {
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README.en.md",
                    filename = "README.en.md",
                    priority = globalPriority + localPriority++
                ))
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README.EN.md",
                    filename = "README.EN.md",
                    priority = globalPriority + localPriority++
                ))
                attempts.add(ReadmeAttempt(
                    path = "${pathPrefix}README-EN.md",
                    filename = "README-EN.md",
                    priority = globalPriority + localPriority++
                ))
            }

            globalPriority += 100 * (pathIndex + 1)
        }

        return attempts.sortedBy { it.priority }
    }

    fun detectReadmeLanguage(content: String): String? = ContentLanguageDetector.detect(content)
}

