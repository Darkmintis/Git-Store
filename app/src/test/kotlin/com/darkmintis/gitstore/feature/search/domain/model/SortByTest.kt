package com.darkmintis.gitstore.feature.search.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SortByTest {

    @Test
    fun `most stars maps to github params`() {
        assertEquals("stars" to "desc", SortBy.MostStars.toGithubParams())
    }

    @Test
    fun `most forks maps to github params`() {
        assertEquals("forks" to "desc", SortBy.MostForks.toGithubParams())
    }

    @Test
    fun `best match has null sort key`() {
        assertEquals(null to "desc", SortBy.BestMatch.toGithubParams())
    }
}
