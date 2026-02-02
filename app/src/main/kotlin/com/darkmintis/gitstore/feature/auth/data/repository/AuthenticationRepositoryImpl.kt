package com.darkmintis.gitstore.feature.auth.data.repository

import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.darkmintis.gitstore.core.data.data_source.TokenDataSource
import com.darkmintis.gitstore.core.domain.model.DeviceStart
import com.darkmintis.gitstore.core.domain.model.DeviceTokenSuccess
import com.darkmintis.gitstore.feature.auth.data.network.GitHubAuthApi
import com.darkmintis.gitstore.feature.auth.data.getGithubClientId
import com.darkmintis.gitstore.feature.auth.domain.repository.AuthenticationRepository

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeoutException

class AuthenticationRepositoryImpl(
    private val tokenDataSource: TokenDataSource,
) : AuthenticationRepository {

    override val accessTokenFlow: Flow<String?>
        get() = tokenDataSource.tokenFlow.map { it?.accessToken }

    override val isAuthenticatedFlow: Flow<Boolean>
        get() = tokenDataSource.tokenFlow.map { it != null }

    override suspend fun isAuthenticated(): Boolean =
        tokenDataSource.current() != null

    override suspend fun startDeviceFlow(): DeviceStart =
        withContext(Dispatchers.IO) {
            val clientId = getGithubClientId()
            require(clientId.isNotBlank()) {
                "Missing GitHub CLIENT_ID. Add GITHUB_CLIENT_ID to local.properties."
            }

            try {
                val result = GitHubAuthApi.startDeviceFlow(clientId)
                Logger.d { "✅ Device flow started. User code: ${result.userCode}" }
                result
            } catch (e: Exception) {
                Logger.d { "❌ Failed to start device flow: ${e.message}" }
                throw Exception(
                    "Failed to start GitHub authentication. " +
                            "Please check your internet connection and try again.",
                    e
                )
            }
        }

    override suspend fun awaitDeviceToken(start: DeviceStart): DeviceTokenSuccess =
        withContext(Dispatchers.IO) {
            val clientId = getGithubClientId()
            val timeoutMs = start.expiresInSec * 1000L
            val startTime = DateClock.System.now().toEpochMilliseconds()

            val initialJitter = (0..2000).random().toLong()
            delay(initialJitter)

            var pollingInterval = (start.intervalSec.coerceAtLeast(5)) * 1000L
            var consecutiveNetworkErrors = 0
            var consecutiveUnknownErrors = 0
            var slowDownCount = 0

            Logger.d { "⏱️ Polling started. Timeout: ${start.expiresInSec}s, Interval: ${start.intervalSec}s" }

            while (isActive) {
                if (DateClock.System.now().toEpochMilliseconds() - startTime >= timeoutMs) {
                    throw TimeoutException(
                        "Authentication timed out after ${start.expiresInSec} seconds. Please try again."
                    )
                }

                try {
                    val res = GitHubAuthApi.pollDeviceToken(clientId, start.deviceCode)
                    val success = res.getOrNull()

                    if (success != null) {
                        Logger.d { "✅ Token received! Saving..." }

                        saveTokenWithVerification(success)

                        Logger.d { "✅ Token saved and verified successfully!" }
                        return@withContext success
                    }

                    val error = res.exceptionOrNull()
                    val errorMsg = (error?.message ?: "").lowercase()

                    when {
                        "authorization_pending" in errorMsg -> {
                            consecutiveNetworkErrors = 0
                            consecutiveUnknownErrors = 0
                            if (slowDownCount > 0) slowDownCount--

                            Logger.d { "📡 Waiting for user authorization..." }
                            delay(pollingInterval + (0..1000).random())
                        }

                        "slow_down" in errorMsg -> {
                            consecutiveNetworkErrors = 0
                            consecutiveUnknownErrors = 0
                            slowDownCount++
                            pollingInterval += 5000

                            Logger.d { "⚠️ Rate limited. New interval: ${pollingInterval}ms (slowdown #$slowDownCount)" }

                            if (slowDownCount > 10) {
                                throw Exception(
                                    "GitHub is experiencing high traffic. Please wait a few minutes and try again."
                                )
                            }

                            delay(pollingInterval + (0..3000).random())
                        }

                        "access_denied" in errorMsg -> {
                            throw Exception(
                                "Authentication was denied. Please try again if this was a mistake."
                            )
                        }

                        "expired_token" in errorMsg ||
                                "expired_device_code" in errorMsg ||
                                "token_expired" in errorMsg -> {
                            throw Exception(
                                "Authorization code expired. Please try again."
                            )
                        }

                        "bad_verification_code" in errorMsg ||
                                "incorrect_device_code" in errorMsg -> {
                            throw Exception(
                                "Invalid verification code. Please restart authentication."
                            )
                        }

                        isNetworkError(errorMsg) -> {
                            consecutiveNetworkErrors++
                            consecutiveUnknownErrors = 0

                            Logger.d { "⚠️ Network error ($consecutiveNetworkErrors/8): $errorMsg" }

                            if (consecutiveNetworkErrors >= 8) {
                                throw Exception(
                                    "Network connection is unstable. Please check your connection and try again."
                                )
                            }

                            val backoff = minOf(
                                pollingInterval * (1 + consecutiveNetworkErrors),
                                30_000L
                            )
                            delay(backoff)
                        }

                        else -> {
                            consecutiveUnknownErrors++
                            Logger.d { "⚠️ Unknown error ($consecutiveUnknownErrors/5): $errorMsg" }

                            if (consecutiveUnknownErrors >= 5) {
                                throw Exception(
                                    "Authentication failed: ${error?.message ?: "Unknown error"}"
                                )
                            }

                            val backoff = minOf(
                                pollingInterval * (1 + consecutiveUnknownErrors / 2),
                                20_000L
                            )
                            delay(backoff)
                        }
                    }

                } catch (e: CancellationException) {
                    throw e
                } catch (e: TimeoutException) {
                    throw e
                } catch (e: Exception) {
                    consecutiveUnknownErrors++
                    Logger.d { "❌ Unexpected error ($consecutiveUnknownErrors/5): ${e.message}" }

                    if (consecutiveUnknownErrors >= 5) {
                        throw Exception(
                            "Authentication failed after multiple errors: ${e.message}",
                            e
                        )
                    }

                    delay(minOf(pollingInterval * 2, 15_000L))
                }
            }

            throw CancellationException("Authentication was cancelled")
        }

    private suspend fun saveTokenWithVerification(token: DeviceTokenSuccess) {
        repeat(5) { attempt ->
            try {
                tokenDataSource.save(token)

                delay(100)
                val saved = tokenDataSource.current()

                if (saved?.accessToken == token.accessToken) {
                    return
                } else {
                    Logger.d { "⚠️ Token verification failed (attempt ${attempt + 1}/5)" }
                    if (attempt == 4) {
                        throw Exception("Token was not persisted correctly after 5 attempts")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.d { "⚠️ Token save failed (attempt ${attempt + 1}/5): ${e.message}" }
                if (attempt == 4) {
                    throw Exception("Failed to save authentication token: ${e.message}", e)
                }
                delay(500L * (attempt + 1))
            }
        }
    }

    private fun isNetworkError(errorMsg: String): Boolean {
        return errorMsg.contains("unable to resolve") ||
                errorMsg.contains("no address") ||
                errorMsg.contains("failed to connect") ||
                errorMsg.contains("connection refused") ||
                errorMsg.contains("network is unreachable") ||
                errorMsg.contains("timeout") ||
                errorMsg.contains("timed out") ||
                errorMsg.contains("connection reset") ||
                errorMsg.contains("broken pipe") ||
                errorMsg.contains("host unreachable") ||
                errorMsg.contains("network error")
    }
}

