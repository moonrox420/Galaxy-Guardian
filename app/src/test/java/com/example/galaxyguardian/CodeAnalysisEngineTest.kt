package com.example.galaxyguardian

import com.example.galaxyguardian.data.service.CodeAnalysisEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeAnalysisEngineTest {

    @Test
    fun testFormatCode_normalizesSpacingAndRemovesTrailingWhitespace() {
        val messyCode = "def test( a,b ):\n    val=1 + 2   \n    return val\n"
        val formatted = CodeAnalysisEngine.formatCode(messyCode)

        val lines = formatted.lines()
        for (line in lines) {
            assertFalse("Line should not end with trailing whitespace: '$line'", line.endsWith(" "))
        }
    }

    @Test
    fun testAnalyzeStyle_detectsWildcardImports() {
        val codeWithWildcard = "from math import *\n\ndef calculate():\n    return sin(0.5)\n"
        val (issues, _) = CodeAnalysisEngine.analyzeStyle(codeWithWildcard)

        assertTrue(
            "Style issues should flag wildcard import",
            issues.any { it.message.contains("Wildcard import", ignoreCase = true) }
        )
    }

    @Test
    fun testAnalyzeSecurity_detectsDangerousEval() {
        val dangerousCode = """
            def run_user_input(payload: str):
                return eval(payload)
        """.trimIndent()

        val (findings, _) = CodeAnalysisEngine.scanSecurityPatterns(dangerousCode)
        assertTrue(
            "Security scanner must detect eval() usage",
            findings.any { it.title.contains("Dynamic Code Evaluation", ignoreCase = true) }
        )
    }

    @Test
    fun testFullAnalysis_calculatesHonestSecurityScore() {
        val cleanCode = """
            import logging
            from typing import Dict, Any

            def process_metrics(data: Dict[str, Any]) -> int:
                \"\"\"Process valid input dictionary.\"\"\"
                return len(data)
        """.trimIndent()

        val cleanAnalysis = CodeAnalysisEngine.analyze(cleanCode)
        assertEquals(100, cleanAnalysis.securityScore)
        assertTrue(cleanAnalysis.securityVulnerabilities.isEmpty())
    }
}
