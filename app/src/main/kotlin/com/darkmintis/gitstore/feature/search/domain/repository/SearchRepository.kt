package com.darkmintis.gitstore.feature.search.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.feature.home.domain.model.PaginatedRepos
import com.darkmintis.gitstore.feature.search.domain.model.ProgrammingLanguage
import com.darkmintis.gitstore.feature.search.domain.model.SearchPlatformType

interface SearchRepository {
    fun searchRepositories(
        query: String,
        searchPlatformType: SearchPlatformType,
        language: ProgrammingLanguage,
        page: Int
    ): Flow<PaginatedRepos>
}

