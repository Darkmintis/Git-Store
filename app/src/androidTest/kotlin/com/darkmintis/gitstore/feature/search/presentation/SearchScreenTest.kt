package com.darkmintis.gitstore.feature.search.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.darkmintis.gitstore.R
import com.darkmintis.gitstore.core.presentation.theme.GithubStoreTheme
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun idleSearch_showsPrompt() {
        composeRule.setContent {
            GithubStoreTheme {
                SearchScreen(
                    state = SearchState(
                        query = "",
                        repositories = emptyList(),
                        isLoading = false,
                        errorMessage = null
                    ),
                    onAction = {}
                )
            }
        }

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.search_idle_title))
            .assertIsDisplayed()
    }

    @Test
    fun emptyResults_showsGuidanceMessage() {
        composeRule.setContent {
            GithubStoreTheme {
                SearchScreen(
                    state = SearchState(
                        query = "gitstore",
                        repositories = emptyList(),
                        isLoading = false,
                        errorMessage = null
                    ),
                    onAction = {}
                )
            }
        }

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.search_try_different_filters))
            .assertIsDisplayed()
    }
}
