package com.darkmintis.gitstore.core.data.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MyMemoryTranslationSupportTest {

    @Test
    fun `rejects MyMemory query length errors`() {
        val error = "QUERY LENGTH LIMIT EXCEEDED. MAX ALLOWED QUERY : 500 CHARS"
        assertFalse(MyMemoryTranslationSupport.isUsableTranslation(error, "Hello world"))
    }

    @Test
    fun `rejects MyMemory quota warnings`() {
        val error = "MYMEMORY WARNING: YOU USED ALL AVAILABLE FREE TRANSLATIONS FOR TODAY"
        assertFalse(MyMemoryTranslationSupport.isUsableTranslation(error, "Hello"))
    }

    @Test
    fun `accepts normal translation`() {
        assertTrue(MyMemoryTranslationSupport.isUsableTranslation("Bonjour", "Hello"))
    }

    @Test
    fun `chunks long text under query limit`() {
        val text = "word ".repeat(120).trim()
        val chunks = MyMemoryTranslationSupport.chunkForQuery(text)
        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= MyMemoryTranslationSupport.MAX_QUERY_CHARS })
        assertEquals(text, chunks.joinToString(""))
    }

    @Test
    fun `groups markdown prose into blocks not lines`() {
        val md = """
            # Title
            Paragraph one line.
            Paragraph two line.

            ```
            code
            ```

            - bullet
        """.trimIndent()
        val segments = MyMemoryTranslationSupport.splitMarkdownSegments(md)
        val translatable = segments.filter { it.translatable }
        assertTrue(translatable.size < md.lines().size)
        assertTrue(translatable.any { it.text.contains("Paragraph one") && it.text.contains("Paragraph two") })
    }
}
