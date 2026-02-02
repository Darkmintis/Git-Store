package com.darkmintis.gitstore.feature.starred_repos.presentation.mappers

import com.darkmintis.gitstore.core.data.local.db.entities.StarredRepo
import com.darkmintis.gitstore.feature.starred_repos.presentation.model.StarredRepositoryUi

fun StarredRepo.toStarredRepositoryUi(isFavorite: Boolean = false) = StarredRepositoryUi(
    repoId = repoId,
    repoName = repoName,
    repoOwner = repoOwner,
    repoOwnerAvatarUrl = repoOwnerAvatarUrl,
    repoDescription = repoDescription,
    primaryLanguage = primaryLanguage,
    repoUrl = repoUrl,
    stargazersCount = stargazersCount,
    forksCount = forksCount,
    openIssuesCount = openIssuesCount,
    isInstalled = isInstalled,
    isFavorite = isFavorite,
    latestRelease = latestVersion,
    latestReleaseUrl = latestReleaseUrl,
    starredAt = starredAt
)

