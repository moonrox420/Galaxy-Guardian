package com.example.galaxyguardian.data.service.analyzers

import com.example.galaxyguardian.data.model.LintIssue
import com.example.galaxyguardian.data.model.QualityAnalysis
import com.example.galaxyguardian.data.model.SecurityVulnerability
import com.example.galaxyguardian.data.model.Severity
import com.example.galaxyguardian.data.model.TargetLanguage
import com.example.galaxyguardian.data.model.TypeIssue
import com.example.galaxyguardian.data.service.CodeAnalysisEngine

interface CodeAnalyzer {
    val supportedLanguages: Set<TargetLanguage>
    fun analyze(code: String): QualityAnalysis
}

class PythonCodeAnalyzer : CodeAnalyzer {
    override val supportedLanguages: Set<TargetLanguage> = setOf(TargetLanguage.PYTHON)

    override fun analyze(code: String): QualityAnalysis {
        return CodeAnalysisEngine.analyze(code)
    }
}

class KotlinCodeAnalyzer : CodeAnalyzer {
    override val supportedLanguages: Set<TargetLanguage> = setOf(TargetLanguage.KOTLIN_ANDROID)

    override fun analyze(code: String): QualityAnalysis {
        val lintIssues = mutableListOf<LintIssue>()
        val typeIssues = mutableListOf<TypeIssue>()
        val vulnerabilities = mutableListOf<SecurityVulnerability>()
        val lines = code.lines()

        lines.forEachIndexed { index, line ->
            val lineNum = index + 1
            val trimmed = line.trim()

            // Check GlobalScope usage
            if (trimmed.contains("GlobalScope.")) {
                lintIssues.add(
                    LintIssue(
                        line = lineNum,
                        ruleCode = "KT-001",
                        message = "Avoid GlobalScope usage. Use lifecycle-aware CoroutineScope or viewModelScope.",
                        severity = Severity.HIGH
                    )
                )
            }

            // Check double-bang (!!) operator
            if (trimmed.contains("!!")) {
                lintIssues.add(
                    LintIssue(
                        line = lineNum,
                        ruleCode = "KT-002",
                        message = "Unsafe non-null assertion (!!) used. Prefer safe calls (?.) or elvis operator (?:).",
                        severity = Severity.MEDIUM
                    )
                )
            }

            // Check hardcoded secret tokens in Kotlin
            val secretPatterns = listOf(
                Regex("""(?i)(password|secret|api_key|token)\s*=\s*['\"][a-zA-Z0-9_\-]{8,}['\"]"""),
                Regex("""AIzaSy[a-zA-Z0-9_-]{33}""")
            )
            for (pattern in secretPatterns) {
                if (pattern.containsMatchIn(trimmed) && !trimmed.contains("BuildConfig") && !trimmed.contains("System.getenv")) {
                    vulnerabilities.add(
                        SecurityVulnerability(
                            id = "KSEC-001",
                            cwe = "CWE-798",
                            title = "Hardcoded Secret Token in Kotlin Source",
                            description = "Sensitive credential string hardcoded directly in code.",
                            line = lineNum,
                            severity = Severity.HIGH,
                            recommendation = "Load credentials securely from BuildConfig or encrypted DataStore/Keystore."
                        )
                    )
                    break
                }
            }

            // Check cleartext HTTP URLs
            if (trimmed.contains("http://") && !trimmed.contains("localhost") && !trimmed.contains("10.0.2.2") && !trimmed.contains("127.0.0.1")) {
                vulnerabilities.add(
                    SecurityVulnerability(
                        id = "KSEC-002",
                        cwe = "CWE-319",
                        title = "Cleartext HTTP URL in Android Endpoint",
                        description = "Using unencrypted HTTP connection endpoints violates Android network security policy.",
                        line = lineNum,
                        severity = Severity.MEDIUM,
                        recommendation = "Use HTTPS endpoints or configure cleartext domain overrides."
                    )
                )
            }

            // Check Compose state hoisting / ViewModel creation in Composable
            if (trimmed.contains("@Composable") && trimmed.contains("viewModel(")) {
                typeIssues.add(
                    TypeIssue(
                        line = lineNum,
                        symbol = "@Composable",
                        message = "ViewModel instantiated directly in Composable. Consider hoisting ViewModel state to screen entry point.",
                        severity = Severity.LOW
                    )
                )
            }
        }

        var secScore = 100
        for (v in vulnerabilities) {
            secScore -= when (v.severity) {
                Severity.CRITICAL -> 25
                Severity.HIGH -> 15
                Severity.MEDIUM -> 8
                Severity.LOW -> 3
                Severity.INFO -> 0
            }
        }
        secScore = secScore.coerceIn(0, 100)

        val lintSummary = if (lintIssues.isEmpty()) {
            "✓ 0 Kotlin style issues detected. Clean coroutine & state practices."
        } else {
            "Found ${lintIssues.size} Kotlin style recommendations."
        }

        val typeSummary = if (typeIssues.isEmpty()) {
            "✓ Type safety check clean."
        } else {
            "Found ${typeIssues.size} Compose & type structure suggestions."
        }

        val secSummary = if (vulnerabilities.isEmpty()) {
            "✓ 0 Android security pattern risks detected."
        } else {
            "Detected ${vulnerabilities.size} security findings in Kotlin code."
        }

        return QualityAnalysis(
            formattedCode = code,
            lintIssues = lintIssues,
            lintSummaryText = lintSummary,
            typeIssues = typeIssues,
            typeSummaryText = typeSummary,
            securityVulnerabilities = vulnerabilities,
            securitySummaryText = secSummary,
            securityScore = secScore
        )
    }
}

object AnalyzerRegistry {
    private val pythonAnalyzer = PythonCodeAnalyzer()
    private val kotlinAnalyzer = KotlinCodeAnalyzer()

    fun getAnalyzer(language: TargetLanguage): CodeAnalyzer {
        return when (language) {
            TargetLanguage.PYTHON -> pythonAnalyzer
            TargetLanguage.KOTLIN_ANDROID -> kotlinAnalyzer
        }
    }
}
