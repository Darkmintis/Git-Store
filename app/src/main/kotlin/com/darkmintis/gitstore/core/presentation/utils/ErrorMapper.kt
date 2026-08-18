package com.darkmintis.gitstore.core.presentation.utils

import com.darkmintis.gitstore.R
import com.darkmintis.gitstore.feature.auth.data.MissingGithubClientIdException
import com.darkmintis.gitstore.network.HttpApiException
import com.darkmintis.gitstore.network.RateLimitException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Maps throwables to friendly, localized user messages.
 * Callers must rethrow [kotlinx.coroutines.CancellationException] before using this.
 */
object ErrorMapper {

    fun message(throwable: Throwable, strings: StringProvider): String =
        message(throwable, strings, R.string.error_unknown)

    fun message(
        throwable: Throwable,
        strings: StringProvider,
        fallbackResId: Int
    ): String {
        return when (val root = unwrap(throwable)) {
            is RateLimitException -> strings.getString(R.string.error_rate_limit)

            is HttpApiException -> httpMessage(root.statusCode, strings)

            is HttpRequestTimeoutException,
            is SocketTimeoutException,
            is TimeoutException -> strings.getString(R.string.error_timeout)

            is UnresolvedAddressException,
            is UnknownHostException,
            is ConnectException -> strings.getString(R.string.error_offline)

            is MissingGithubClientIdException -> strings.getString(R.string.error_auth_not_configured)

            is IOException -> strings.getString(R.string.error_network)

            else -> {
                parseHttpFromMessage(root.message)?.let { code ->
                    return httpMessage(code, strings)
                }
                if (looksLikeDownloadFailure(root.message)) {
                    return strings.getString(R.string.error_download_failed)
                }
                strings.getString(fallbackResId)
            }
        }
    }

    private fun httpMessage(statusCode: Int, strings: StringProvider): String = when (statusCode) {
        401 -> strings.getString(R.string.error_unauthorized)
        403 -> strings.getString(R.string.error_forbidden)
        404 -> strings.getString(R.string.error_not_found)
        in 500..599 -> strings.getString(R.string.error_server)
        else -> strings.getString(R.string.error_http_generic, statusCode)
    }

    private fun unwrap(throwable: Throwable): Throwable {
        var current = throwable
        var depth = 0
        while (current.cause != null && current.cause !== current && depth < 5) {
            // Prefer typed network/API causes when wrapped
            val cause = current.cause ?: break
            if (
                cause is RateLimitException ||
                cause is HttpApiException ||
                cause is MissingGithubClientIdException ||
                cause is IOException ||
                cause is HttpRequestTimeoutException ||
                cause is TimeoutException
            ) {
                return cause
            }
            current = cause
            depth++
        }
        return throwable
    }

    private fun parseHttpFromMessage(message: String?): Int? {
        if (message.isNullOrBlank()) return null
        val match = Regex("""HTTP\s+(\d{3})""", RegexOption.IGNORE_CASE).find(message)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun looksLikeDownloadFailure(message: String?): Boolean {
        if (message.isNullOrBlank()) return false
        val lower = message.lowercase()
        return lower.contains("download failed") ||
            lower.contains("downloadmanager") ||
            Regex("""download failed:\s*\d+""").containsMatchIn(lower)
    }
}
