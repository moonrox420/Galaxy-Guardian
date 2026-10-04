package com.example.galaxyguardian

import com.example.galaxyguardian.data.service.llm.MultiFileProjectParser
import org.junit.Assert.assertEquals
import org.junit.Test

class MultiFileProjectParserTest {

    @Test
    fun testParse_singleFile_returnsOneFile() {
        val raw = """
            fun main() {
                println("Hello Galaxy")
            }
        """.trimIndent()

        val files = MultiFileProjectParser.parse(raw, "Main.kt")
        assertEquals(1, files.size)
        assertEquals("Main.kt", files[0].path)
        assertEquals(raw, files[0].content)
    }

    @Test
    fun testParse_multiFile_splitsCorrectly() {
        val raw = """
            // File: ui/HomeScreen.kt
            package com.example.app
            
            class HomeScreen
            
            // File: ui/HomeViewModel.kt
            package com.example.app
            
            class HomeViewModel
        """.trimIndent()

        val files = MultiFileProjectParser.parse(raw)
        assertEquals(2, files.size)
        assertEquals("ui/HomeScreen.kt", files[0].path)
        assertEquals("ui/HomeViewModel.kt", files[1].path)
        assertEquals("package com.example.app\n\nclass HomeScreen", files[0].content)
        assertEquals("package com.example.app\n\nclass HomeViewModel", files[1].content)
    }
}
