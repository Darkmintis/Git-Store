package com.darkmintis.gitstore.feature.search.presentation

import com.darkmintis.gitstore.core.presentation.model.DiscoveryRepository
import com.darkmintis.gitstore.feature.search.domain.model.ProgrammingLanguage
import com.darkmintis.gitstore.feature.search.domain.model.SortBy

data class SearchState(
    val query: String = "",
    val repositories: List<DiscoveryRepository> = emptyList(),
    val selectedSortBy: SortBy = SortBy.BestMatch,
    val selectedLanguage: ProgrammingLanguage = ProgrammingLanguage.All,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePages: Boolean = true,
    val totalCount: Int? = null,
    val isLanguageSheetVisible: Boolean = false,
    val isSortSheetVisible: Boolean = false
)


