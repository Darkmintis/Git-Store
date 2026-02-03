package com.darkmintis.gitstore.feature.search.presentation

import com.darkmintis.gitstore.core.domain.model.GithubRepoSummary
import com.darkmintis.gitstore.feature.search.domain.model.ProgrammingLanguage
import com.darkmintis.gitstore.feature.search.domain.model.SearchPlatformType
import com.darkmintis.gitstore.feature.search.domain.model.SortBy

sealed interface SearchAction {
    data class OnSearchChange(val query: String) : SearchAction
    data class OnPlatformTypeSelected(val searchPlatformType: SearchPlatformType) : SearchAction
    data class OnLanguageSelected(val language: ProgrammingLanguage) : SearchAction
    data class OnSortBySelected(val sortBy: SortBy) : SearchAction
    data class OnRepositoryClick(val repository: GithubRepoSummary) : SearchAction
    data class OnRepositoryDeveloperClick(val username: String) : SearchAction
    data class OnToggleFavorite(val repository: GithubRepoSummary) : SearchAction
    data object OnSearchImeClick : SearchAction
    data object OnNavigateBackClick : SearchAction
    data object LoadMore : SearchAction
    data object Retry : SearchAction
    data object OnToggleLanguageSheetVisibility : SearchAction
}


