package com.example.galaxyguardian

import com.example.galaxyguardian.data.model.TargetLanguage
import com.example.galaxyguardian.data.service.analyzers.AnalyzerRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinCodeAnalyzerTest {

    private val analyzer = AnalyzerRegistry.getAnalyzer(TargetLanguage.KOTLIN_ANDROID)

    @Test
    fun testKotlinAnalyzer_detectsGlobalScopeAndDoubleBang() {
        val badKotlin = """
            package com.example.app

            fun badFunction(str: String?) {
                GlobalScope.launch {
                    val len = str!!.length
                }
            }
        """.trimIndent()

        val analysis = analyzer.analyze(badKotlin)

        assertTrue(
            "Analyzer should detect GlobalScope usage",
            analysis.lintIssues.any { it.ruleCode == "KT-001" }
        )
        assertTrue(
            "Analyzer should detect double-bang operator",
            analysis.lintIssues.any { it.ruleCode == "KT-002" }
        )
    }

    @Test
    fun testKotlinAnalyzer_cleanCodeScores100() {
        val cleanKotlin = """
            package com.example.app

            import androidx.compose.runtime.Composable
            import androidx.compose.material3.Text

            @Composable
            fun SafeScreen(title: String) {
                Text(text = title)
            }
        """.trimIndent()

        val analysis = analyzer.analyze(cleanKotlin)

        assertEquals(100, analysis.securityScore)
        assertTrue(analysis.securityVulnerabilities.isEmpty())
    }
}
