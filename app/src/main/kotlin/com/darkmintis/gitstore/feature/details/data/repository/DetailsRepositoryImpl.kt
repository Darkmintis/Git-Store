package com.darkmintis.gitstore.feature.details.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import com.darkmintis.gitstore.app.app_state.AppStateManager
import com.darkmintis.gitstore.core.data.services.LocalizationManager
import com.darkmintis.gitstore.core.domain.model.GithubRelease
import com.darkmintis.gitstore.core.domain.model.GithubRepoSummary
import com.darkmintis.gitstore.core.domain.model.GithubUser
import com.darkmintis.gitstore.core.domain.model.GithubUserProfile
import com.darkmintis.gitstore.feature.details.data.dto.ReleaseNetwork
import com.darkmintis.gitstore.feature.details.data.dto.RepoByIdNetwork
import com.darkmintis.gitstore.feature.details.data.dto.RepoInfoNetwork
import com.darkmintis.gitstore.feature.details.data.dto.UserProfileNetwork
import com.darkmintis.gitstore.feature.details.data.mappers.toDomain
import com.darkmintis.gitstore.feature.details.data.utils.ReadmeLocalizationHelper
import com.darkmintis.gitstore.feature.details.data.utils.preprocessMarkdown
import com.darkmintis.gitstore.feature.details.domain.model.RepoStats
import com.darkmintis.gitstore.feature.details.domain.repository.DetailsRepository
import com.darkmintis.gitstore.network.RateLimitException
import com.darkmintis.gitstore.network.safeApiCall

class DetailsRepositoryImpl(
    private val github: HttpClient,
    private val appStateManager: AppStateManager,
    private val localizationManager: LocalizationManager
) : DetailsRepository {

    private val readmeHelper = ReadmeLocalizationHelper(localizationManager)

    override suspend fun getRepositoryById(id: Long): GithubRepoSummary {
        val repoResult = github.safeApiCall<RepoByIdNetwork>(
            rateLimitHandler = appStateManager.rateLimitHandler,
            autoRetryOnRateLimit = false
        ) {
            get("/repositories/$id") {
                header(HttpHeaders.Accept, "application/vnd.github+json")
            }
        }

        val repo = repoResult.getOrElse { error ->
            if (error is RateLimitException) {
                appStateManager.updateRateLimit(error.rateLimitInfo)
            }
            throw error
        }

        return GithubRepoSummary(
            id = repo.id,
            name = repo.name,
            fullName = repo.fullName,
            owner = GithubUser(
                id = repo.owner.id,
                login = repo.owner.login,
                avatarUrl = repo.owner.avatarUrl,
                htmlUrl = repo.owner.htmlUrl
            ),
            description = repo.description,
            htmlUrl = repo.htmlUrl,
            stargazersCount = repo.stars,
            forksCount = repo.forks,
            language = repo.language,
            topics = repo.topics,
            releasesUrl = "https://api.github.com/repos/${repo.owner.login}/${repo.name}/releases{/id}",
            updatedAt = repo.updatedAt,
            defaultBranch = repo.defaultBranch
        )
    }

    override suspend fun getLatestPublishedRelease(
        owner: String,
        repo: String,
        defaultBranch: String
    ): GithubRelease? {
        val releasesResult = github.safeApiCall<List<ReleaseNetwork>>(
            rateLimitHandler = appStateManager.rateLimitHandler,
            autoRetryOnRateLimit = false
        ) {
            get("/repos/$owner/$repo/releases") {
                header(HttpHeaders.Accept, "application/vnd.github+json")
                parameter("per_page", 10)
            }
        }

        releasesResult.onFailure { error ->
            if (error is RateLimitException) {
                appStateManager.updateRateLimit(error.rateLimitInfo)
            }
        }

        val releases = releasesResult.getOrNull() ?: return null

        val latest = releases
            .asSequence()
            .filter { (it.draft != true) && (it.prerelease != true) }
            .sortedByDescending { it.publishedAt ?: it.createdAt ?: "" }
            .firstOrNull()
            ?: return null

        val processedLatestRelease = latest.copy(
            body = latest.body?.replace("<details>", "")
                ?.replace("</details>", "")
                ?.replace("<summary>", "")
                ?.replace("</summary>", "")
                ?.replace("\r\n", "\n")
                ?.let { rawMarkdown ->
                    preprocessMarkdown(
                        markdown = rawMarkdown,
                        baseUrl = "https://raw.githubusercontent.com/$owner/$repo/${defaultBranch}/"
                    )
                }
        )

        return processedLatestRelease.toDomain()
    }

    override suspend fun getReadme(
        owner: String,
        repo: String,
        defaultBranch: String
    ): Triple<String, String?, String>? {
        val attempts = readmeHelper.generateReadmeAttempts()
        val baseUrl = "https://raw.githubusercontent.com/$owner/$repo/$defaultBranch/"
        val primaryLang = localizationManager.getPrimaryLanguageCode()

        Logger.d {
            "Attempting to fetch README for language preference: ${localizationManager.getCurrentLanguageCode()}"
        }

        val foundReadmes = coroutineScope {
            attempts.map { attempt ->
                async(start = CoroutineStart.LAZY) {
                    try {
                        Logger.d { "Trying ${attempt.path} (priority: ${attempt.priority})..." }

                        val rawMarkdownResult = github.safeApiCall<String>(
                            rateLimitHandler = appStateManager.rateLimitHandler,
                            autoRetryOnRateLimit = false
                        ) {
                            get("$baseUrl${attempt.path}")
                        }

                        rawMarkdownResult.onFailure { error ->
                            if (error is RateLimitException) {
                                appStateManager.updateRateLimit(error.rateLimitInfo)
                            }
                        }

                        val rawMarkdown = rawMarkdownResult.getOrNull()

                        if (rawMarkdown != null) {
                            Logger.d { "Successfully fetched ${attempt.path}" }

                            val processed = preprocessMarkdown(
                                markdown = rawMarkdown,
                                baseUrl = baseUrl
                            )

                            val detectedLang = readmeHelper.detectReadmeLanguage(processed)
                            Logger.d { "Detected language: ${detectedLang ?: "unknown"} for ${attempt.path}" }

                            attempt to Pair(processed, detectedLang)
                        } else {
                            null
                        }
                    } catch (e: Throwable) {
                        Logger.d { "Failed to fetch ${attempt.path}: ${e.message}" }
                        null
                    }
                }
            }.also { asyncTasks ->
                asyncTasks.take(6).forEach { it.start() }
            }.awaitAll()
                .filterNotNull()
                .associateBy({ it.first }, { it.second })
        }

        if (foundReadmes.isEmpty()) {
            Logger.e { "Failed to fetch any README variant." }
            return null
        }

        foundReadmes.entries.firstOrNull { (attempt, content) ->
            attempt.filename != "README.md" && content.second == primaryLang
        }?.let { (attempt, content) ->
            Logger.d { "Found localized README matching user language: ${attempt.path}" }
            return Triple(content.first, content.second, attempt.path)
        }

        foundReadmes.entries.firstOrNull { (attempt, _) ->
            attempt.filename.contains(".${primaryLang}.", ignoreCase = true) ||
                    attempt.filename.contains("-${primaryLang.uppercase()}.", ignoreCase = true)
        }?.let { (attempt, content) ->
            Logger.d { "Found explicit language file for user: ${attempt.path}" }
            return Triple(content.first, content.second ?: primaryLang, attempt.path)
        }

        foundReadmes.entries.firstOrNull { (attempt, content) ->
            attempt.filename == "README.md" && content.second == primaryLang
        }?.let { (attempt, content) ->
            Logger.d { "Default README matches user language: ${attempt.path}" }
            return Triple(content.first, content.second, attempt.path)
        }

        if (primaryLang == "en") {
            foundReadmes.entries.firstOrNull { (_, content) ->
                content.second == "en"
            }?.let { (attempt, content) ->
                Logger.d { "Found English README for English user: ${attempt.path}" }
                return Triple(content.first, content.second, attempt.path)
            }
        }

        foundReadmes.entries.firstOrNull { (_, content) ->
            content.second == primaryLang
        }?.let { (attempt, content) ->
            Logger.d { "Fallback: Using README matching user language: ${attempt.path}" }
            return Triple(content.first, content.second, attempt.path)
        }

        if (primaryLang == "en") {
            foundReadmes.entries.firstOrNull { (_, content) ->
                content.second == "en"
            }?.let { (attempt, content) ->
                Logger.d { "Fallback: Using English README: ${attempt.path}" }
                return Triple(content.first, content.second, attempt.path)
            }
        }

        foundReadmes.entries.firstOrNull { (attempt, _) ->
            attempt.path == "README.md"
        }?.let { (attempt, content) ->
            Logger.d { "Fallback: Using root README.md (language: ${content.second}): ${attempt.path}" }
            return Triple(content.first, content.second, attempt.path)
        }

        foundReadmes.entries.firstOrNull { (attempt, _) ->
            attempt.path.startsWith(".github/")
        }?.let { (attempt, content) ->
            Logger.d { "Fallback: Using .github README: ${attempt.path}" }
            return Triple(content.first, content.second, attempt.path)
        }

        foundReadmes.entries.minByOrNull { it.key.priority }?.let { (attempt, content) ->
            Logger.d { "Fallback: Using highest priority README: ${attempt.path}" }
            return Triple(content.first, content.second, attempt.path)
        }

        return null
    }

    override suspend fun getRepoStats(owner: String, repo: String): RepoStats {
        val infoResult = github.safeApiCall<RepoInfoNetwork>(
            rateLimitHandler = appStateManager.rateLimitHandler,
            autoRetryOnRateLimit = false
        ) {
            get("/repos/$owner/$repo") {
                header(HttpHeaders.Accept, "application/vnd.github+json")
            }
        }

        val info = infoResult.getOrElse { error ->
            if (error is RateLimitException) {
                appStateManager.updateRateLimit(error.rateLimitInfo)
            }
            throw error
        }

        return RepoStats(
            stars = info.stars,
            forks = info.forks,
            openIssues = info.openIssues,
        )
    }

    override suspend fun getUserProfile(username: String): GithubUserProfile {
        val userResult = github.safeApiCall<UserProfileNetwork>(
            rateLimitHandler = appStateManager.rateLimitHandler,
            autoRetryOnRateLimit = false
        ) {
            get("/users/$username") {
                header(HttpHeaders.Accept, "application/vnd.github+json")
            }
        }

        val user = userResult.getOrElse { error ->
            if (error is RateLimitException) {
                appStateManager.updateRateLimit(error.rateLimitInfo)
            }
            throw error
        }

        return GithubUserProfile(
            id = user.id,
            login = user.login,
            name = user.name,
            bio = user.bio,
            avatarUrl = user.avatarUrl,
            htmlUrl = user.htmlUrl,
            followers = user.followers,
            following = user.following,
            publicRepos = user.publicRepos,
            location = user.location,
            company = user.company,
            blog = user.blog,
            twitterUsername = user.twitterUsername
        )
    }
}

