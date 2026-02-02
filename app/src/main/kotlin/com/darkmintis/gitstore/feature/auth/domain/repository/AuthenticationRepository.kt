package com.darkmintis.gitstore.feature.auth.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.core.domain.model.DeviceStart
import com.darkmintis.gitstore.core.domain.model.DeviceTokenSuccess

interface AuthenticationRepository {
    val accessTokenFlow: Flow<String?>
    val isAuthenticatedFlow: Flow<Boolean>

    suspend fun startDeviceFlow(): DeviceStart

    suspend fun awaitDeviceToken(start: DeviceStart): DeviceTokenSuccess

    suspend fun isAuthenticated(): Boolean
}

