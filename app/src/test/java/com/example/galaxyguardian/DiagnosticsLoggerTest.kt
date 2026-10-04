package com.example.galaxyguardian


import com.example.galaxyguardian.data.service.diagnostics.DiagnosticsLogger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsLoggerTest {

    @Test
    fun testDiagnostics_redactsApiKeysAndSecrets() {
        val rawDiagnostics = """
            Config loaded: API_KEY=AIzaSy123456789012345678901234567890123
            GitHub Token: ghp_123456789012345678901234567890123456
        """.trimIndent()



        val report = DiagnosticsLogger.sanitizeSecrets(rawDiagnostics)

        assertFalse("Secrets must be redacted", report.contains("AIzaSy123456789012345678901234567890123"))
        assertFalse("GitHub tokens must be redacted", report.contains("ghp_123456789012345678901234567890123456"))
        assertTrue("Report must indicate redaction", report.contains("REDACTED"))
    }
}
