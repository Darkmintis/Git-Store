package com.darkmintis.gitstore.feature.apps.presentation

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.darkmintis.gitstore.R
import com.darkmintis.gitstore.core.presentation.theme.GithubStoreTheme
import org.junit.Rule
import org.junit.Test

class AppsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyApps_showsGuidance() {
        composeRule.setContent {
            GithubStoreTheme {
                AppsScreen(
                    state = AppsState(
                        apps = emptyList(),
                        isLoading = false
                    ),
                    onAction = {},
                    snackbarHostState = remember { SnackbarHostState() }
                )
            }
        }

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.no_apps_found))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.apps_empty_message))
            .assertIsDisplayed()
    }
}
