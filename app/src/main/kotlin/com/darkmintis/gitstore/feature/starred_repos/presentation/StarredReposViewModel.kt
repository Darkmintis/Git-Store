
package com.darkmintis.gitstore.feature.starred_repos.presentation

import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant

import com.darkmintis.gitstore.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.app.Application
import com.darkmintis.gitstore.core.data.data_source.TokenDataSource
import com.darkmintis.gitstore.core.data.local.db.entities.FavoriteRepo
import com.darkmintis.gitstore.core.domain.repository.FavouritesRepository
import com.darkmintis.gitstore.core.domain.repository.StarredRepository
import com.darkmintis.gitstore.feature.starred_repos.presentation.mappers.toStarredRepositoryUi

class StarredReposViewModel (
    private val application: Application,
    private val starredRepository: StarredRepository,
    private val favouritesRepository: FavouritesRepository,
    private val tokenDataSource: TokenDataSource
) : ViewModel() {
    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(StarredReposState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                checkAuthAndLoad()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = StarredReposState()
        )

    private fun checkAuthAndLoad() {
        viewModelScope.launch {
            val isAuthenticated = tokenDataSource.current()?.accessToken != null

            _state.update { it.copy(isAuthenticated = isAuthenticated) }

            if (isAuthenticated) {
                loadStarredRepos()
                syncIfNeeded()
            }
        }
    }

    private fun loadStarredRepos() {
        viewModelScope.launch {
            combine(
                starredRepository.getAllStarred(),
                favouritesRepository.getAllFavorites()
            ) { starred, favorites ->
                val favoriteIds = favorites.map { it.repoId }.toSet()

                starred.map {
                    it.toStarredRepositoryUi(
                        isFavorite = favoriteIds.contains(it.repoId)
                    )
                }
            }
                .flowOn(Dispatchers.Default)
                .collect { starredRepos ->
                    _state.update {
                        it.copy(
                            starredRepositories = starredRepos.toImmutableList(),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun syncIfNeeded() {
        viewModelScope.launch {
            if (starredRepository.needsSync()) {
                syncStarredRepos()
            } else {
                val lastSync = starredRepository.getLastSyncTime()
                _state.update { it.copy(lastSyncTime = lastSync) }
            }
        }
    }

    private fun syncStarredRepos(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, errorMessage = null) }

            val result = starredRepository.syncStarredRepos(forceRefresh)

            result
                .onSuccess {
                    val lastSync = starredRepository.getLastSyncTime()
                    _state.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncTime = lastSync
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSyncing = false,
                            errorMessage = error.message ?: application.getString(R.string.sync_starred_failed)
                        )
                    }
                }
        }
    }

    fun onAction(action: StarredReposAction) {
        when (action) {
            StarredReposAction.OnNavigateBackClick -> {
                // Handled in composable
            }

            is StarredReposAction.OnRepositoryClick -> {
                // Handled in composable
            }

            is StarredReposAction.OnDeveloperProfileClick -> {
                // Handled in composable
            }

            StarredReposAction.OnRefresh -> {
                syncStarredRepos(forceRefresh = true)
            }

            StarredReposAction.OnRetrySync -> {
                syncStarredRepos(forceRefresh = true)
            }

            StarredReposAction.OnDismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }

            is StarredReposAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    val repo = action.repository

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





