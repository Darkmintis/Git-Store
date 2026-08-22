package com.darkmintis.gitstore.app.navigation

import com.darkmintis.gitstore.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.vector.ImageVector

fun <T> SnapshotStateList<T>.replaceAll(element: T) {
    clear()
    add(element)
}

data class BottomNavigationItem(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: GithubStoreGraph
)

object BottomNavigationUtils {
    fun items(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                titleRes = R.string.home,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                screen = GithubStoreGraph.HomeScreen
            ),
            BottomNavigationItem(
                titleRes = R.string.favourites,
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder,
                screen = GithubStoreGraph.FavouritesScreen
            ),
            BottomNavigationItem(
                titleRes = R.string.downloads,
                selectedIcon = Icons.Filled.Download,
                unselectedIcon = Icons.Filled.Download,
                screen = GithubStoreGraph.DownloadsScreen
            ),
            BottomNavigationItem(
                titleRes = R.string.search,
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                screen = GithubStoreGraph.SearchScreen
            )
        )
    }

    fun allowedScreens(): List<GithubStoreGraph> {
        return listOf(
            GithubStoreGraph.HomeScreen,
            GithubStoreGraph.FavouritesScreen,
            GithubStoreGraph.DownloadsScreen,
            GithubStoreGraph.SearchScreen
        )
    }
}
