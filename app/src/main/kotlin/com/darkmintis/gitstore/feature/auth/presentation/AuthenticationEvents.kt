package com.darkmintis.gitstore.feature.auth.presentation

sealed interface AuthenticationEvents {
    data object OnNavigateToMain : AuthenticationEvents
}

