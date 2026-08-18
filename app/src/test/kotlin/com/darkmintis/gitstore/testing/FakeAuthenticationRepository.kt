package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.domain.model.DeviceStart
import com.darkmintis.gitstore.core.domain.model.DeviceTokenSuccess
import com.darkmintis.gitstore.feature.auth.domain.repository.AuthenticationRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthenticationRepository(
    initialToken: String? = null
) : AuthenticationRepository {
    private val token = MutableStateFlow(initialToken)
    var startCalls = 0
    var awaitCalls = 0
    var nextStart: DeviceStart = DeviceStart(
        deviceCode = "device",
        userCode = "ABCD-EFGH",
        verificationUri = "https://github.com/login/device",
        verificationUriComplete = "https://github.com/login/device?user_code=ABCD-EFGH",
        intervalSec = 1,
        expiresInSec = 900
    )
    var nextToken: DeviceTokenSuccess = DeviceTokenSuccess(
        accessToken = "token",
        tokenType = "bearer",
        scope = "repo,user"
    )
    var failStartWith: Throwable? = null
    var failAwaitWith: Throwable? = null
    var awaitGate: CompletableDeferred<Unit>? = null

    override val accessTokenFlow: Flow<String?> = token
    override val isAuthenticatedFlow: Flow<Boolean> =
        MutableStateFlow(initialToken != null)

    override suspend fun startDeviceFlow(): DeviceStart {
        startCalls++
        failStartWith?.let { throw it }
        return nextStart
    }

    override suspend fun awaitDeviceToken(start: DeviceStart): DeviceTokenSuccess {
        awaitCalls++
        failAwaitWith?.let { throw it }
        awaitGate?.await()
        token.value = nextToken.accessToken
        return nextToken
    }

    override suspend fun isAuthenticated(): Boolean = !token.value.isNullOrEmpty()
}
