package com.darkmintis.gitstore.app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.darkmintis.gitstore.core.domain.getPlatform
import com.darkmintis.gitstore.core.domain.model.PlatformType

@Composable
fun BottomNavigation(
    currentScreen: GithubStoreGraph,
    onNavigate: (GithubStoreGraph) -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentScreen in BottomNavigationUtils.allowedScreens()) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp
        ) {
            BottomNavigationUtils
                .items()
                .filterNot {
                    getPlatform().type != PlatformType.ANDROID &&
                            it.screen == GithubStoreGraph.AppsScreen
                }
                .forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.iconRes,
                                contentDescription = stringResource(item.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.titleRes),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = item.screen == currentScreen,
                        onClick = {
                            onNavigate(item.screen)
                        }
                    )
                }
        }
    }
}


