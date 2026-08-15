package com.darkmintis.gitstore.network

import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RateLimitInfoTest {

    @Test
    fun `parses reset header as epoch seconds`() {
        val info = RateLimitInfo.fromHeaders(
            headersOf(
                "X-RateLimit-Limit" to listOf("60"),
                "X-RateLimit-Remaining" to listOf("0"),
                "X-RateLimit-Reset" to listOf("1700000000")
            )
        )

        assertNotNull(info)
        assertEquals(1_700_000_000L, info.reset.epochSeconds)
        assertEquals(0, info.remaining)
    }
}
