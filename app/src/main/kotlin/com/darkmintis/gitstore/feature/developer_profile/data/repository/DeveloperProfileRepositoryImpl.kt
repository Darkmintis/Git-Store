package com.darkmintis.gitstore.feature.developer_profile.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.darkmintis.gitstore.core.data.local.db.dao.InstalledAppDao
import com.darkmintis.gitstore.core.domain.Platform
import com.darkmintis.gitstore.core.domain.model.PlatformType
import com.darkmintis.gitstore.core.domain.repository.FavouritesRepository
import com.darkmintis.gitstore.feature.developer_profile.data.dto.GitHubRepoResponse
import com.darkmintis.gitstore.feature.developer_profile.data.dto.GitHubUserResponse
import com.darkmintis.gitstore.feature.developer_profile.data.mappers.toDomain
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperProfile
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperRepository
import com.darkmintis.gitstore.feature.developer_profile.domain.repository.DeveloperProfileRepository

class DeveloperProfileRepositoryImpl(
    private val httpClient: HttpClient,
    private val platform: Platform,
    private val installedAppsDao: InstalledAppDao,
    private val favouritesRepository: FavouritesRepository
) : DeveloperProfileRepository {

    override suspend fun getDeveloperProfile(username: String): Result<DeveloperProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = httpClient.get("/users/$username")

                if (!response.status.isSuccess()) {
                    return@withContext Result.failure(
                        Exception("Failed to fetch developer profile: ${response.status.description}")
                    )
                }

                val userResponse: GitHubUserResponse = response.body()
                Result.success(userResponse.toDomain())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e(e) { "Failed to fetch developer profile for $username" }
                Result.failure(e)
            }
        }
    }

    override suspend fun getDeveloperRepositories(username: String): Result<List<DeveloperRepository>> {
        return withContext(Dispatchers.IO) {
            try {
                val allRepos = mutableListOf<GitHubRepoResponse>()
                var page = 1
                val perPage = 100

                while (true) {
                    val response = httpClient.get("/users/$username/repos") {
                        parameter("per_page", perPage)
                        parameter("page", page)
                        parameter("type", "owner")
                        parameter("sort", "updated")
                        parameter("direction", "desc")
                    }

                    if (!response.status.isSuccess()) {
                        return@withContext Result.failure(
                            Exception("Failed to fetch repositories: ${response.status.description}")
                        )
                    }

                    val repos: List<GitHubRepoResponse> = response.body()

                    if (repos.isEmpty()) break

                    allRepos.addAll(repos.filter { !it.archived && !it.fork })

                    if (repos.size < perPage) break
                    page++
                }

                val allFavorites = favouritesRepository.getAllFavorites().first()
                val favoriteIds = allFavorites.map { it.repoId }.toSet()

                val processedRepos = coroutineScope {
                    val semaphore = Semaphore(20)
                    val deferredResults = allRepos.map { repo ->
                        async {
                            semaphore.withPermit {
                                processRepository(repo, favoriteIds)
                            }
                        }
                    }
                    deferredResults.awaitAll()
                }

                Result.success(processedRepos)
            }  catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e(e) { "Failed to fetch repositories for $username" }
                Result.failure(e)
            }
        }
    }

    private suspend fun processRepository(
        repo: GitHubRepoResponse,
        favoriteIds: Set<Long>
    ): DeveloperRepository {
        val installedApp = installedAppsDao.getAppByRepoId(repo.id)
        val isFavorite = favoriteIds.contains(repo.id)

        val (hasReleases, hasInstallableAssets, latestVersion) = checkReleaseInfo(
            owner = repo.fullName.split("/")[0],
            repoName = repo.name
        )

        return repo.toDomain(
            hasReleases = hasReleases,
            hasInstallableAssets = hasInstallableAssets,
            isInstalled = installedApp != null,
            isFavorite = isFavorite,
            latestVersion = latestVersion
        )
    }

    private suspend fun checkReleaseInfo(
        owner: String,
        repoName: String
    ): Triple<Boolean, Boolean, String?> {
        return try {
            val response = httpClient.get("/repos/$owner/$repoName/releases") {
                parameter("per_page", 10)
            }

            if (!response.status.isSuccess()) {
                return Triple(false, false, null)
            }

            val releases: List<ReleaseNetworkModel> = response.body()

            val stableRelease = releases.firstOrNull {
                it.draft != true && it.prerelease != true
            }

            if (stableRelease == null) {
                return Triple(releases.isNotEmpty(), false, null)
            }

            val hasInstallableAssets = stableRelease.assets.any { asset ->
                val name = asset.name.lowercase()
                name.endsWith(".apk")
            }

            Triple(
                true,
                hasInstallableAssets,
                if (hasInstallableAssets) stableRelease.tagName else null
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w(e) { "Failed to check releases for $owner/$repoName" }
            Triple(false, false, null)
        }
    }

    @Serializable
    private data class ReleaseNetworkModel(
        val assets: List<AssetNetworkModel>,
        val draft: Boolean? = null,
        val prerelease: Boolean? = null,
        @SerialName("tag_name") val tagName: String,
        @SerialName("published_at") val publishedAt: String? = null
    )

    @Serializable
    private data class AssetNetworkModel(
        val name: String
    )
}

