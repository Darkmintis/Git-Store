package com.darkmintis.gitstore.app.app_state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.darkmintis.gitstore.core.data.data_source.TokenDataSource
import com.darkmintis.gitstore.network.RateLimitHandler
import com.darkmintis.gitstore.network.RateLimitInfo

class AppStateManager(
    val rateLimitHandler: RateLimitHandler,
    val tokenDataSource: TokenDataSource
) {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            tokenDataSource.tokenFlow.collect { token ->
                val isAuth = token != null
                _appState.update { it.copy(isAuthenticated = isAuth) }

                if (isAuth) {
                    rateLimitHandler.lastKnownRateLimit = null
                    updateRateLimit(null)
                }
            }
        }
    }

    fun updateRateLimit(rateLimitInfo: RateLimitInfo?) {
        _appState.update { currentState ->
            val shouldShowDialog = if (rateLimitInfo?.isExhausted == true) {
                true
            } else {
                currentState.showRateLimitDialog
            }

            currentState.copy(
                rateLimitInfo = rateLimitInfo,
                showRateLimitDialog = shouldShowDialog
            )
        }
    }

    fun dismissRateLimitDialog() {
        _appState.update { it.copy(showRateLimitDialog = false) }
    }
}

