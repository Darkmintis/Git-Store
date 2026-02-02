package com.darkmintis.gitstore.feature.auth.presentation

import com.darkmintis.gitstore.core.domain.model.DeviceStart

data class AuthenticationState(
    val loginState: AuthLoginState = AuthLoginState.LoggedOut,
    val copied: Boolean = false,
    val info: String? = null
)

sealed interface AuthLoginState {
    data object LoggedOut : AuthLoginState
    data class DevicePrompt(
        val start: DeviceStart,
    ) : AuthLoginState

    data object Pending : AuthLoginState
    data object LoggedIn : AuthLoginState
    data class Error(val message: String) : AuthLoginState
}

