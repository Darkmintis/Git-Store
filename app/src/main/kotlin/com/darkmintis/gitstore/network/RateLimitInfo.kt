package com.darkmintis.gitstore.network

import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant

data class RateLimitInfo(
    val limit: Int,
    val remaining: Int,
    val reset: DateInstant,
    val resource: String = "core"
) {
    val isExhausted: Boolean get() = remaining == 0

    fun timeUntilReset(): Long {
        val now = DateClock.System.now()
        return ((reset - now).inWholeMilliseconds).toLong().coerceAtLeast(0)
    }

    companion object {
        fun fromHeaders(headers: io.ktor.http.Headers): RateLimitInfo? {
            return try {
                val limit = headers["X-RateLimit-Limit"]?.toIntOrNull() ?: return null
                val remaining = headers["X-RateLimit-Remaining"]?.toIntOrNull() ?: return null
                val reset = headers["X-RateLimit-Reset"]?.toLongOrNull()?.let {
                    DateInstant.fromEpochMilliseconds(it)
                } ?: return null
                val resource = headers["X-RateLimit-Resource"] ?: "core"

                RateLimitInfo(limit, remaining, reset, resource)
            } catch (e: Exception) {
                null
            }
        }
    }
}

class RateLimitException(
    val rateLimitInfo: RateLimitInfo,
    message: String
) : Exception(message)

class RateLimitHandler {
    var lastKnownRateLimit: RateLimitInfo? = null

    fun isRateLimited(): Boolean {
        val info = lastKnownRateLimit ?: return false
        if (!info.isExhausted) return false

        return info.timeUntilReset() > 0
    }

    fun getTimeUntilReset(): Long {
        return lastKnownRateLimit?.timeUntilReset() ?: 0L
    }

    fun updateFromHeaders(headers: io.ktor.http.Headers) {
        RateLimitInfo.fromHeaders(headers)?.let {
            lastKnownRateLimit = it
        }
    }

    fun getCurrentRateLimit(): RateLimitInfo? = lastKnownRateLimit
}






