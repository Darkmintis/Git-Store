package com.darkmintis.gitstore.feature.favourites.presentation

import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.darkmintis.gitstore.core.data.local.db.entities.FavoriteRepo
import com.darkmintis.gitstore.core.domain.repository.FavouritesRepository
import com.darkmintis.gitstore.feature.favourites.presentation.mappers.toFavouriteRepositoryUi

class FavouritesViewModel(
    application: Application,
    private val favouritesRepository: FavouritesRepository
) : AndroidViewModel(application) {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(FavouritesState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadFavouriteRepos()

                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = FavouritesState()
        )

    private fun loadFavouriteRepos() {
        viewModelScope.launch {
            favouritesRepository
                .getAllFavorites()
                .map { it.map { it.toFavouriteRepositoryUi(getApplication()) } }
                .flowOn(Dispatchers.Default)
                .collect { favoriteRepos ->
                    _state.update { it.copy(
                        favouriteRepositories = favoriteRepos.toImmutableList()
                    ) }
                }
        }
    }

        fun onAction(action: FavouritesAction) {
        when (action) {
            FavouritesAction.OnNavigateBackClick -> {
                // Handled in composable
            }

            is FavouritesAction.OnRepositoryClick -> {
                // Handled in composable
            }

            is FavouritesAction.OnDeveloperProfileClick -> {
                // Handled in composable
            }

            is FavouritesAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    val repo = action.favouriteRepository

                    val favoriteRepo = FavoriteRepo(
                        repoId = repo.repoId,
                        repoName = repo.repoName,
                        repoOwner = repo.repoOwner,
                        repoOwnerAvatarUrl = repo.repoOwnerAvatarUrl,
                        repoDescription = repo.repoDescription,
                        primaryLanguage = repo.primaryLanguage,
                        repoUrl = repo.repoUrl,
                        latestVersion = repo.latestRelease,
                        latestReleaseUrl = repo.latestReleaseUrl,
                        addedAt = DateClock.System.now().toEpochMilliseconds(),
                        lastSyncedAt = DateClock.System.now().toEpochMilliseconds()
                    )

                    favouritesRepository.toggleFavorite(favoriteRepo)
                }
            }
        }
    }

}




