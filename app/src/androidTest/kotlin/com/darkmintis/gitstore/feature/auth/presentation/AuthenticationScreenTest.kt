package com.darkmintis.gitstore.feature.auth.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.darkmintis.gitstore.R
import com.darkmintis.gitstore.core.presentation.theme.GithubStoreTheme
import org.junit.Rule
import org.junit.Test

class AuthenticationScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loggedOut_showsSignInCallToAction() {
        composeRule.setContent {
            GithubStoreTheme {
                AuthenticationScreen(
                    state = AuthenticationState(loginState = AuthLoginState.LoggedOut),
                    onAction = {}
                )
            }
        }

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.sign_in_with_github))
            .assertIsDisplayed()
    }
}
