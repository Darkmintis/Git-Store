package com.darkmintis.gitstore.feature.auth.presentation

import android.app.Application
import com.darkmintis.gitstore.R

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger



import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.darkmintis.gitstore.core.domain.model.DeviceStart
import com.darkmintis.gitstore.core.presentation.utils.BrowserHelper
import com.darkmintis.gitstore.core.presentation.utils.ClipboardHelper
import com.darkmintis.gitstore.feature.auth.domain.repository.AuthenticationRepository

class AuthenticationViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val browserHelper: BrowserHelper,
    private val clipboardHelper: ClipboardHelper,
    private val scope: CoroutineScope,
    private val application: Application
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state: MutableStateFlow<AuthenticationState> =
        MutableStateFlow(AuthenticationState())

    private val _events = Channel<AuthenticationEvents>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                scope.launch {
                    authenticationRepository.accessTokenFlow.collect { token ->
                        _state.update {
                            it.copy(
                                loginState = if (token.isNullOrEmpty()) {
                                    AuthLoginState.LoggedOut
                                } else {
                                    _events.trySend(AuthenticationEvents.OnNavigateToMain)
                                    AuthLoginState.LoggedIn
                                }
                            )
                        }
                    }
                }

                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AuthenticationState()
        )

    fun onAction(action: AuthenticationAction) {
        when (action) {
            is AuthenticationAction.StartLogin -> startLogin()
            is AuthenticationAction.CopyCode -> copyCode(action.start)
            is AuthenticationAction.OpenGitHub -> openGitHub(action.start)
            AuthenticationAction.MarkLoggedIn -> _state.update { it.copy(loginState = AuthLoginState.LoggedIn) }
            AuthenticationAction.MarkLoggedOut -> _state.update { it.copy(loginState = AuthLoginState.LoggedOut) }
            is AuthenticationAction.OnInfo -> {
                _state.update {
                    it.copy(
                        info = action.message
                    )
                }
            }
        }
    }

    private fun startLogin() {
        viewModelScope.launch {
            try {
                val start = withContext(Dispatchers.IO) {
                    authenticationRepository.startDeviceFlow()
                }

                withContext(Dispatchers.Main.immediate) {
                    _state.update {
                        it.copy(
                            loginState = AuthLoginState.DevicePrompt(start),
                            copied = false
                        )
                    }

                    try {
                        clipboardHelper.copy(
                            label = application.getString(R.string.enter_code_on_github),
                            text = start.userCode
                        )
                        _state.update { it.copy(copied = true) }
                    } catch (e: Exception) {
                        Logger.d { "⚠️ Failed to copy to clipboard: ${e.message}" }
                    }
                }

                withContext(Dispatchers.IO) {
                    authenticationRepository.awaitDeviceToken(start = start)
                }

                withContext(Dispatchers.Main.immediate) {
                    _state.update { it.copy(loginState = AuthLoginState.LoggedIn) }
                    _events.trySend(AuthenticationEvents.OnNavigateToMain)
                }

            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                withContext(Dispatchers.Main.immediate) {
                    _state.update {
                        it.copy(
                            loginState = AuthLoginState.Error(
                                t.message ?: application.getString(R.string.error_unknown)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun openGitHub(start: DeviceStart) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            try {
                val url = start.verificationUriComplete ?: start.verificationUri
                browserHelper.openUrl(url)
            } catch (e: Exception) {
                Logger.d { "⚠️ Failed to open browser: ${e.message}" }
            }
        }
    }

    private fun copyCode(start: DeviceStart) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            try {
                clipboardHelper.copy(
                    label = "GitHub Code",
                    text = start.userCode
                )

                _state.update {
                    it.copy(
                        loginState = AuthLoginState.DevicePrompt(start),
                        copied = true
                    )
                }
            } catch (e: Exception) {
                Logger.d { "⚠️ Failed to copy to clipboard: ${e.message}" }
                _state.update {
                    it.copy(
                        loginState = AuthLoginState.DevicePrompt(start),
                        copied = false
                    )
                }
            }
        }
    }
}




