package com.darkmintis.gitstore.app.navigation

import com.darkmintis.gitstore.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.GetApp

data class BottomNavigationItem(
    val titleRes: Int,
    val iconRes: ImageVector,
    val screen: GithubStoreGraph
)

object BottomNavigationUtils {
    fun items(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                titleRes = R.string.home,
                iconRes = Icons.Filled.Home,
                screen = GithubStoreGraph.HomeScreen
            ),
            BottomNavigationItem(
                titleRes = R.string.favourites,
                iconRes = Icons.Filled.Favorite,
                screen = GithubStoreGraph.FavouritesScreen
            ),
            BottomNavigationItem(
                titleRes = R.string.installed_apps,
                iconRes = Icons.Filled.GetApp,
                screen = GithubStoreGraph.AppsScreen
            ),
            BottomNavigationItem(
                titleRes = R.string.search,
                iconRes = Icons.Filled.Search,
                screen = GithubStoreGraph.SearchScreen
            )
        )
    }

    fun allowedScreens(): List<GithubStoreGraph> {
        return listOf(
            GithubStoreGraph.HomeScreen,
            GithubStoreGraph.SearchScreen,
            GithubStoreGraph.AppsScreen,
            GithubStoreGraph.FavouritesScreen
        )
    }
}



