package com.darkmintis.gitstore.feature.favourites.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.darkmintis.gitstore.R
import com.darkmintis.gitstore.core.presentation.theme.GithubStoreTheme
import org.junit.Rule
import org.junit.Test

class FavouritesScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyFavourites_showsGuidance() {
        composeRule.setContent {
            GithubStoreTheme {
                FavouritesScreen(
                    state = FavouritesState(
                        favouriteRepositories = emptyList(),
                        isLoading = false
                    ),
                    onAction = {}
                )
            }
        }

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.no_favorite_repos))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.favourites_empty_message))
            .assertIsDisplayed()
    }
}
