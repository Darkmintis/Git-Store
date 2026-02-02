package com.darkmintis.gitstore.feature.developer_profile.data.mappers

import com.darkmintis.gitstore.feature.developer_profile.data.dto.GitHubRepoResponse
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperRepository

fun GitHubRepoResponse.toDomain(
    hasReleases: Boolean = false,
    hasInstallableAssets: Boolean = false,
    isInstalled: Boolean = false,
    isFavorite: Boolean = false,
    latestVersion: String? = null
) = DeveloperRepository(
    id = id,
    name = name,
    fullName = fullName,
    description = description,
    htmlUrl = htmlUrl,
    stargazersCount = stargazersCount,
    forksCount = forksCount,
    openIssuesCount = openIssuesCount,
    language = language,
    hasReleases = hasReleases,
    hasInstallableAssets = hasInstallableAssets,
    isInstalled = isInstalled,
    isFavorite = isFavorite,
    latestVersion = latestVersion,
    updatedAt = updatedAt,
    pushedAt = pushedAt
)

