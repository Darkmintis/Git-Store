package com.darkmintis.gitstore.feature.search.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.feature.home.domain.model.PaginatedRepos
import com.darkmintis.gitstore.feature.search.domain.model.ProgrammingLanguage

interface SearchRepository {
    fun searchRepositories(
        query: String,
        language: ProgrammingLanguage,
        page: Int
    ): Flow<PaginatedRepos>
}

