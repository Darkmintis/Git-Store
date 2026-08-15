package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.data.local.db.entities.FavoriteRepo
import com.darkmintis.gitstore.core.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavouritesRepository(
    initial: List<FavoriteRepo> = emptyList()
) : FavouritesRepository {
    private val favorites = MutableStateFlow(initial)
    val toggled = mutableListOf<FavoriteRepo>()

    override fun getAllFavorites(): Flow<List<FavoriteRepo>> = favorites

    override fun isFavorite(repoId: Long): Flow<Boolean> =
        favorites.map { list -> list.any { it.repoId == repoId } }

    override suspend fun isFavoriteSync(repoId: Long): Boolean =
        favorites.value.any { it.repoId == repoId }

    override suspend fun addFavorite(repo: FavoriteRepo) {
        favorites.value = favorites.value.filterNot { it.repoId == repo.repoId } + repo
    }

    override suspend fun removeFavorite(repoId: Long) {
        favorites.value = favorites.value.filterNot { it.repoId == repoId }
    }

    override suspend fun toggleFavorite(repo: FavoriteRepo) {
        toggled += repo
        if (favorites.value.any { it.repoId == repo.repoId }) {
            removeFavorite(repo.repoId)
        } else {
            addFavorite(repo)
        }
    }

    override suspend fun updateFavoriteInstallStatus(
        repoId: Long,
        installed: Boolean,
        packageName: String?
    ) = Unit

    override suspend fun syncFavoriteVersions() = Unit
}
