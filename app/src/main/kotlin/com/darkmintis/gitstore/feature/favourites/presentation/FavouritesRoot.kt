package com.darkmintis.gitstore.feature.favourites.presentation

import com.darkmintis.gitstore.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import com.darkmintis.gitstore.core.presentation.components.EmptyState
import com.darkmintis.gitstore.core.presentation.components.GithubStoreTopBarTitle
import com.darkmintis.gitstore.core.presentation.components.LoadingState
import com.darkmintis.gitstore.core.presentation.theme.GithubStoreTheme
import com.darkmintis.gitstore.feature.favourites.presentation.components.FavouriteRepositoryItem

@Composable
fun FavouritesRoot(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (repoId: Long) -> Unit,
    onNavigateToDeveloperProfile: (username: String) -> Unit,
    viewModel: FavouritesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FavouritesScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is FavouritesAction.OnRepositoryClick -> {
                    onNavigateToDetails(action.favouriteRepository.repoId)
                }

                is FavouritesAction.OnDeveloperProfileClick -> {
                    onNavigateToDeveloperProfile(action.username)
                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        }
    )
}

@Composable
fun FavouritesScreen(
    state: FavouritesState,
    onAction: (FavouritesAction) -> Unit,
) {
    Scaffold(
        topBar = {
            FavouritesTopbar(onAction)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && state.favouriteRepositories.isEmpty() -> {
                    LoadingState()
                }

                state.favouriteRepositories.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.no_favorite_repos),
                        message = stringResource(R.string.favourites_empty_message),
                        icon = Icons.Outlined.FavoriteBorder
                    )
                }

                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(
                            350.dp
                        ),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.favouriteRepositories,
                            key = { it.repoId }
                        ) { repo ->
                            FavouriteRepositoryItem(
                                favouriteRepository = repo,
                                onToggleFavouriteClick = {
                                    onAction(FavouritesAction.OnToggleFavorite(repo))
                                },
                                onItemClick = {
                                    onAction(FavouritesAction.OnRepositoryClick(repo))
                                },
                                onDevProfileClick = {
                                    onAction(FavouritesAction.OnDeveloperProfileClick(repo.repoOwner))
                                },
                                modifier = Modifier.Companion.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FavouritesTopbar(
    onAction: (FavouritesAction) -> Unit,
) {
    TopAppBar(
        title = {
            GithubStoreTopBarTitle(
                text = stringResource(R.string.favourites)
            )
        }
    )
}


@Preview
@Composable
private fun Preview() {
    GithubStoreTheme {
        FavouritesScreen(
            state = FavouritesState(),
            onAction = {}
        )
    }
}




