
package com.darkmintis.gitstore.feature.starred_repos.presentation

import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant

import com.darkmintis.gitstore.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import com.darkmintis.gitstore.core.presentation.theme.GithubStoreTheme
import com.darkmintis.gitstore.feature.starred_repos.presentation.components.StarredRepositoryItem

@Composable
fun StarredReposRoot(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (repoId: Long) -> Unit,
    onNavigateToDeveloperProfile: (username: String) -> Unit,
    viewModel: StarredReposViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StarredScreen(
        state = state,
        onAction = { action ->
            when (action) {
                StarredReposAction.OnNavigateBackClick -> onNavigateBack()
                is StarredReposAction.OnRepositoryClick -> onNavigateToDetails(action.repository.repoId)
                is StarredReposAction.OnDeveloperProfileClick -> onNavigateToDeveloperProfile(action.username)
                else -> viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StarredScreen(
    state: StarredReposState,
    onAction: (StarredReposAction) -> Unit,
) {
    val pullRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredRepos = remember(state.starredRepositories, searchQuery) {
        if (searchQuery.isBlank()) {
            state.starredRepositories
        } else {
            state.starredRepositories.filter { repo ->
                repo.repoName.contains(searchQuery, ignoreCase = true) ||
                        repo.repoOwner.contains(searchQuery, ignoreCase = true) ||
                        repo.repoDescription.orEmpty().contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            StarredTopBar(
                lastSyncTime = state.lastSyncTime,
                isSyncing = state.isSyncing,
                repoCount = state.starredRepositories.size,
                onAction = onAction
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !state.isAuthenticated -> {
                    EmptyStateView(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.sign_in_required),
                        message = stringResource(R.string.sign_in_with_github_for_stars),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.isLoading -> {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.starredRepositories.isEmpty() && !state.isSyncing -> {
                    EmptyStateView(
                        icon = Icons.Outlined.StarOutline,
                        title = stringResource(R.string.no_starred_repos),
                        message = stringResource(R.string.star_repos_hint),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search_starred_repos),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = searchQuery.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.clear_search)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Repo count
                        Text(
                            text = stringResource(
                                R.string.starred_repos_x_repos,
                                state.starredRepositories.size
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        PullToRefreshBox(
                            isRefreshing = state.isSyncing,
                            onRefresh = {
                                onAction(StarredReposAction.OnRefresh)
                            },
                            state = pullRefreshState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (filteredRepos.isEmpty() && searchQuery.isNotBlank()) {
                                EmptyStateView(
                                    icon = Icons.Default.Search,
                                    title = stringResource(R.string.no_repositories_found),
                                    message = stringResource(R.string.search_try_different_filters),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Adaptive(350.dp),
                                    verticalItemSpacing = 12.dp,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 8.dp
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(
                                        items = filteredRepos,
                                        key = { it.repoId }
                                    ) { repo ->
                                        StarredRepositoryItem(
                                            repository = repo,
                                            onToggleFavoriteClick = {
                                                onAction(StarredReposAction.OnToggleFavorite(repo))
                                            },
                                            onItemClick = {
                                                onAction(StarredReposAction.OnRepositoryClick(repo))
                                            },
                                            onDevProfileClick = {
                                                onAction(StarredReposAction.OnDeveloperProfileClick(repo.repoOwner))
                                            },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            state.errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(
                            onClick = {
                                onAction(StarredReposAction.OnRetrySync)
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.retry)
                            )
                        }
                    },
                    dismissAction = {
                        IconButton(
                            onClick = {
                                onAction(StarredReposAction.OnDismissError)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.dismiss)
                            )
                        }
                    }
                ) {
                    Text(
                        text = message
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StarredTopBar(
    lastSyncTime: Long?,
    isSyncing: Boolean,
    repoCount: Int,
    onAction: (StarredReposAction) -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.starred_repositories),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (lastSyncTime != null && !isSyncing) {
                        Text(
                            text = "${stringResource(R.string.last_synced)}:" +
                                    " ${formatRelativeTime(lastSyncTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = { onAction(StarredReposAction.OnNavigateBackClick) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            actions = {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 12.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        )
    }
}

@Composable
private fun formatRelativeTime(timestamp: Long): String {
    val now = DateClock.System.now().toEpochMilliseconds()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> stringResource(R.string.just_now)
        diff < 3600_000 -> stringResource(R.string.minutes_ago, diff / 60_000)
        diff < 86400_000 -> stringResource(R.string.hours_ago, diff / 3600_000)
        else -> stringResource(R.string.days_ago, diff / 86400_000)
    }
}
