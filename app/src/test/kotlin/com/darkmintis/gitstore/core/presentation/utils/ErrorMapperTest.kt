package com.darkmintis.gitstore.core.presentation.utils

import com.darkmintis.gitstore.R
import com.darkmintis.gitstore.network.HttpApiException
import com.darkmintis.gitstore.network.RateLimitException
import com.darkmintis.gitstore.network.RateLimitInfo
import com.darkmintis.gitstore.testing.FakeStringProvider
import kotlinx.datetime.Instant
import java.net.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMapperTest {

    private val strings = FakeStringProvider(
        mapOf(
            R.string.error_rate_limit to "rate-limit",
            R.string.error_timeout to "timeout",
            R.string.error_offline to "offline",
            R.string.error_network to "network",
            R.string.error_unauthorized to "unauthorized",
            R.string.error_forbidden to "forbidden",
            R.string.error_not_found to "not-found",
            R.string.error_server to "server",
            R.string.error_http_generic to "http-%s",
            R.string.error_download_failed to "download-failed",
            R.string.error_unknown to "unknown"
        )
    )

    @Test
    fun `maps rate limit`() {
        val info = RateLimitInfo(60, 0, Instant.fromEpochSeconds(1_700_000_000))
        assertEquals(
            "rate-limit",
            ErrorMapper.message(RateLimitException(info, "x"), strings)
        )
    }

    @Test
    fun `maps http status codes`() {
        assertEquals("unauthorized", ErrorMapper.message(HttpApiException(401, "Unauthorized"), strings))
        assertEquals("forbidden", ErrorMapper.message(HttpApiException(403, "Forbidden"), strings))
        assertEquals("not-found", ErrorMapper.message(HttpApiException(404, "Not Found"), strings))
        assertEquals("server", ErrorMapper.message(HttpApiException(502, "Bad Gateway"), strings))
        assertEquals("http-418", ErrorMapper.message(HttpApiException(418, "Teapot"), strings))
    }

    @Test
    fun `maps offline and timeout`() {
        assertEquals("offline", ErrorMapper.message(UnknownHostException("api.github.com"), strings))
        assertEquals(
            "timeout",
            ErrorMapper.message(java.net.SocketTimeoutException("timeout"), strings)
        )
    }

    @Test
    fun `maps legacy http message and download codes`() {
        assertEquals(
            "forbidden",
            ErrorMapper.message(Exception("HTTP 403: Forbidden"), strings)
        )
        assertEquals(
            "download-failed",
            ErrorMapper.message(Exception("Download failed: 1001"), strings)
        )
    }

    @Test
    fun `falls back to unknown`() {
        assertEquals("unknown", ErrorMapper.message(IllegalStateException("boom"), strings))
    }
}
