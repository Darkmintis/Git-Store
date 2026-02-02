package com.darkmintis.gitstore.core.presentation.model

import com.darkmintis.gitstore.core.domain.model.GithubRepoSummary

data class DiscoveryRepository(
    val isInstalled: Boolean,
    val isUpdateAvailable: Boolean,
    val isFavourite: Boolean,
    val isStarred: Boolean,
    val repository: GithubRepoSummary,
)

