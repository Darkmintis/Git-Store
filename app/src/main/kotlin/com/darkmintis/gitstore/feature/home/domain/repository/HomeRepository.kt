package com.darkmintis.gitstore.feature.home.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.feature.home.domain.model.PaginatedRepos

interface HomeRepository {
    fun getTrendingRepositories(page: Int): Flow<PaginatedRepos>
    fun getNew(page: Int): Flow<PaginatedRepos>
    fun getPopular(page: Int): Flow<PaginatedRepos>
}


