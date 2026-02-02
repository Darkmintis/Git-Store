package com.darkmintis.gitstore.feature.starred_repos.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.darkmintis.gitstore.feature.starred_repos.presentation.model.StarredRepositoryUi

data class StarredReposState(
    val starredRepositories: ImmutableList<StarredRepositoryUi> = persistentListOf(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val lastSyncTime: Long? = null,
    val isAuthenticated: Boolean = false
)

