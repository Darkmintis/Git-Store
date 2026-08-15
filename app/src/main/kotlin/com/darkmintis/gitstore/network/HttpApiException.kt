package com.darkmintis.gitstore.network

/**
 * Typed GitHub/API HTTP failure for user-facing mapping.
 */
class HttpApiException(
    val statusCode: Int,
    val statusDescription: String,
    message: String = "HTTP $statusCode: $statusDescription"
) : Exception(message)
