package com.darkmintis.gitstore.feature.auth.presentation

import app.cash.turbine.test
import com.darkmintis.gitstore.R
import com.darkmintis.gitstore.testing.FakeAuthenticationRepository
import com.darkmintis.gitstore.testing.FakeBrowserHelper
import com.darkmintis.gitstore.testing.FakeClipboardHelper
import com.darkmintis.gitstore.testing.FakeStringProvider
import com.darkmintis.gitstore.testing.MainDispatcherTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationViewModelTest : MainDispatcherTest() {

    private fun createViewModel(
        authRepo: FakeAuthenticationRepository = FakeAuthenticationRepository()
    ): AuthenticationViewModel {
        return AuthenticationViewModel(
            authenticationRepository = authRepo,
            browserHelper = FakeBrowserHelper(),
            clipboardHelper = FakeClipboardHelper(),
            scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Main),
            stringProvider = FakeStringProvider(
                mapOf(
                    R.string.enter_code_on_github to "Enter code",
                    R.string.error_unknown to "Unknown error"
                )
            )
        )
    }

    @Test
    fun `start login moves to device prompt then logged in and emits navigate`() = runTest {
        val authRepo = FakeAuthenticationRepository()
        val viewModel = createViewModel(authRepo)

        viewModel.events.test {
            viewModel.state.test {
                awaitItem()
                viewModel.onAction(AuthenticationAction.StartLogin)

                var sawPrompt = false
                var sawLoggedIn = false
                for (i in 0 until 10) {
                    when (val loginState = awaitItem().loginState) {
                        is AuthLoginState.DevicePrompt -> {
                            sawPrompt = true
                            assertEquals("ABCD-EFGH", loginState.start.userCode)
                        }
                        AuthLoginState.LoggedIn -> sawLoggedIn = true
                        else -> Unit
                    }
                    if (sawPrompt && sawLoggedIn) break
                }
                assertTrue(sawPrompt)
                assertTrue(sawLoggedIn)
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(AuthenticationEvents.OnNavigateToMain, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, authRepo.startCalls)
        assertEquals(1, authRepo.awaitCalls)
    }

    @Test
    fun `start login failure surfaces error state`() = runTest {
        val authRepo = FakeAuthenticationRepository().apply {
            failStartWith = IllegalStateException("device flow failed")
        }
        val viewModel = createViewModel(authRepo)

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(AuthenticationAction.StartLogin)

            var error: AuthLoginState.Error? = null
            for (i in 0 until 10) {
                val loginState = awaitItem().loginState
                if (loginState is AuthLoginState.Error) {
                    error = loginState
                    break
                }
            }
            assertIs<AuthLoginState.Error>(error)
            assertEquals("Unknown error", error.message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
