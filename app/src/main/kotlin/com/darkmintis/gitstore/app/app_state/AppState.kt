package com.darkmintis.gitstore.app.app_state

import com.darkmintis.gitstore.network.RateLimitInfo

data class AppState(
    val rateLimitInfo: RateLimitInfo? = null,
    val showRateLimitDialog: Boolean = false,
    val isAuthenticated: Boolean = false
)


