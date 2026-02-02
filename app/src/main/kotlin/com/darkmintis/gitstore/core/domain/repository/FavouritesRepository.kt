package com.darkmintis.gitstore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.core.data.local.db.entities.FavoriteRepo

interface FavouritesRepository {
    fun getAllFavorites(): Flow<List<FavoriteRepo>>
    fun isFavorite(repoId: Long): Flow<Boolean>
    suspend fun isFavoriteSync(repoId: Long): Boolean
    
    suspend fun addFavorite(repo: FavoriteRepo)
    suspend fun removeFavorite(repoId: Long)
    suspend fun toggleFavorite(repo: FavoriteRepo)
    
    suspend fun updateFavoriteInstallStatus(repoId: Long, installed: Boolean, packageName: String?)
    suspend fun syncFavoriteVersions()
}

