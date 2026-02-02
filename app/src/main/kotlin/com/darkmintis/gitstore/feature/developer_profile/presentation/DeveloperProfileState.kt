package com.darkmintis.gitstore.feature.developer_profile.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperProfile
import com.darkmintis.gitstore.feature.developer_profile.domain.model.DeveloperRepository
import com.darkmintis.gitstore.feature.developer_profile.domain.model.RepoFilterType
import com.darkmintis.gitstore.feature.developer_profile.domain.model.RepoSortType

data class DeveloperProfileState(
    val username: String = "",
    val profile: DeveloperProfile? = null,
    val repositories: ImmutableList<DeveloperRepository> = persistentListOf(),
    val filteredRepositories: ImmutableList<DeveloperRepository> = persistentListOf(),
    val isLoading: Boolean = false,
    val isLoadingRepos: Boolean = false,
    val errorMessage: String? = null,
    val currentFilter: RepoFilterType = RepoFilterType.ALL,
    val currentSort: RepoSortType = RepoSortType.UPDATED,
    val searchQuery: String = ""
)

