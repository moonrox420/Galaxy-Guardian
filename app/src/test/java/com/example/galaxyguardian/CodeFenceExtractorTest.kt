package com.example.galaxyguardian

import com.example.galaxyguardian.data.service.llm.CodeFenceExtractor
import org.junit.Assert.assertEquals
import org.junit.Test

class CodeFenceExtractorTest {

    @Test
    fun testExtract_stripsPythonFence() {
        val input = """
            ```python
            class WeatherBot:
                pass
            ```
        """.trimIndent()

        val clean = CodeFenceExtractor.extractCleanCode(input, "python")
        assertEquals("class WeatherBot:\n    pass", clean)
    }

    @Test
    fun testExtract_stripsKotlinFence() {
        val input = """
            ```kotlin
            class HomeScreen
            ```
        """.trimIndent()

        val clean = CodeFenceExtractor.extractCleanCode(input, "kotlin")
        assertEquals("class HomeScreen", clean)
    }

    @Test
    fun testExtract_stripsBareFence() {
        val input = """
            ```
            val x = 10
            ```
        """.trimIndent()

        val clean = CodeFenceExtractor.extractCleanCode(input, "kotlin")
        assertEquals("val x = 10", clean)
    }
}
