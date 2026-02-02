package com.darkmintis.gitstore.feature.auth.presentation

import com.darkmintis.gitstore.core.domain.model.DeviceStart

sealed interface AuthenticationAction {
    data object StartLogin : AuthenticationAction
    data class CopyCode(val start: DeviceStart) : AuthenticationAction
    data class OpenGitHub(val start: DeviceStart) : AuthenticationAction
    data object MarkLoggedOut : AuthenticationAction
    data object MarkLoggedIn : AuthenticationAction
    data class OnInfo(val message: String) : AuthenticationAction
}

