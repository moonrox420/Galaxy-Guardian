package com.example.galaxyguardian.data.service

import com.example.galaxyguardian.data.model.AnalysisStatus
import com.example.galaxyguardian.data.model.LintIssue
import com.example.galaxyguardian.data.model.QualityAnalysis
import com.example.galaxyguardian.data.model.SecurityVulnerability
import com.example.galaxyguardian.data.model.Severity
import com.example.galaxyguardian.data.model.TypeIssue

object CodeAnalysisEngine {

    fun formatCode(rawCode: String): String {
        if (rawCode.isBlank()) return ""
        val lines = rawCode.lines()
        val formattedLines = mutableListOf<String>()
        var previousWasEmpty = false

        for (line in lines) {
            val trimmedRight = line.trimEnd()
            if (trimmedRight.isBlank()) {
                if (!previousWasEmpty && formattedLines.isNotEmpty()) {
                    formattedLines.add("")
                    previousWasEmpty = true
                }
            } else {
                formattedLines.add(trimmedRight)
                previousWasEmpty = false
            }
        }
        val result = formattedLines.joinToString("\n").trim()
        return if (result.isNotEmpty()) "$result\n" else ""
    }

    fun analyzeStyle(code: String): Pair<List<LintIssue>, String> {
        val issues = mutableListOf<LintIssue>()
        val lines = code.lines()

        lines.forEachIndexed { index, line ->
            val lineNum = index + 1
            val trimmed = line.trim()

            // C0301: Line too long (PEP 8 > 88 characters)
            if (line.length > 88) {
                issues.add(
                    LintIssue(
                        line = lineNum,
                        ruleCode = "C0301",
                        message = "Line too long (${line.length}/88 characters)",
                        severity = Severity.LOW
                    )
                )
            }

            // W0702: Bare except clause
            if (trimmed == "except:" || trimmed.startsWith("except :")) {
                issues.add(
                    LintIssue(
                        line = lineNum,
                        ruleCode = "W0702",
                        message = "No exception type specified (bare 'except:' clause)",
                        severity = Severity.MEDIUM
                    )
                )
            }

            // E0001: Missing colon at end of statement header
            if ((trimmed.startsWith("def ") || trimmed.startsWith("class ")) && !trimmed.endsWith(":")) {
                issues.add(
                    LintIssue(
                        line = lineNum,
                        ruleCode = "E0001",
                        message = "SyntaxError: missing colon at end of statement header",
                        severity = Severity.HIGH
                    )
                )
            }

            // W0401: Wildcard imports
            if (trimmed.startsWith("from ") && trimmed.endsWith("import *")) {
                issues.add(
                    LintIssue(
                        line = lineNum,
                        ruleCode = "W0401",
                        message = "Wildcard import used ('from ... import *')",
                        severity = Severity.LOW
                    )
                )
            }

            // C0103: Variable name naming convention
            val varAssignmentMatch = Regex("""^([A-Z][a-zA-Z0-9_]*)\s*=""").find(trimmed)
            if (varAssignmentMatch != null && !trimmed.startsWith("class ")) {
                val varName = varAssignmentMatch.groupValues[1]
                if (varName != varName.uppercase()) {
                    issues.add(
                        LintIssue(
                            line = lineNum,
                            ruleCode = "C0103",
                            message = "Variable name '$varName' doesn't conform to snake_case style",
                            severity = Severity.LOW
                        )
                    )
                }
            }
        }

        // Style score calculation (0.00 to 10.00)
        val score = (10.0 - (issues.size * 1.5).coerceAtMost(10.0)).coerceAtLeast(0.0)
        val formattedScore = String.format("%.2f", score)

        val stringBuilder = StringBuilder()
        stringBuilder.append("--------------------------------------------------------------------\n")
        stringBuilder.append("Code Style & PEP 8 Quality Report - Score: $formattedScore/10.00\n")
        stringBuilder.append("--------------------------------------------------------------------\n")
        if (issues.isEmpty()) {
            stringBuilder.append("✓ 0 style issues detected. Code conforms cleanly to PEP 8 standards.\n")
        } else {
            issues.forEach { issue ->
                stringBuilder.append("[${issue.ruleCode}] Line ${issue.line}: ${issue.message} (${issue.severity})\n")
            }
        }

        return Pair(issues, stringBuilder.toString())
    }

    fun analyzeTypeAnnotations(code: String): Pair<List<TypeIssue>, String> {
        val issues = mutableListOf<TypeIssue>()
        val lines = code.lines()

        lines.forEachIndexed { index, line ->
            val lineNum = index + 1
            val trimmed = line.trim()

            // Function definition missing return type annotation
            if (trimmed.startsWith("def ") && trimmed.contains("(") && trimmed.contains(")")) {
                val signature = trimmed.substringAfter("def ").substringBefore(":")
                val funcName = signature.substringBefore("(")
                val params = signature.substringAfter("(").substringBeforeLast(")")

                if (!signature.contains("->")) {
                    issues.add(
                        TypeIssue(
                            line = lineNum,
                            symbol = funcName,
                            message = "Function '$funcName' is missing a return type annotation (e.g. -> None)",
                            severity = Severity.MEDIUM
                        )
                    )
                }

                // Check untyped parameters
                if (params.isNotBlank() && params != "self" && params != "cls") {
                    val individualParams = params.split(",")
                    for (param in individualParams) {
                        val pName = param.trim().substringBefore("=").trim()
                        if (pName.isNotEmpty() && pName != "self" && pName != "cls" && !param.contains(":")) {
                            issues.add(
                                TypeIssue(
                                    line = lineNum,
                                    symbol = pName,
                                    message = "Parameter '$pName' in function '$funcName' lacks type annotation",
                                    severity = Severity.LOW
                                )
                            )
                        }
                    }
                }
            }
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append("--------------------------------------------------------------------\n")
        stringBuilder.append("Type Annotation Coverage Report\n")
        stringBuilder.append("--------------------------------------------------------------------\n")
        if (issues.isEmpty()) {
            stringBuilder.append("✓ Complete type annotation coverage. All functions and parameters annotated.\n")
        } else {
            stringBuilder.append("Found ${issues.size} type annotation suggestions:\n")
            issues.forEach { issue ->
                stringBuilder.append("Line ${issue.line}: note: [${issue.symbol}] ${issue.message}\n")
            }
        }

        return Pair(issues, stringBuilder.toString())
    }

    fun scanSecurityPatterns(code: String): Pair<List<SecurityVulnerability>, String> {
        val findings = mutableListOf<SecurityVulnerability>()
        val lines = code.lines()

        lines.forEachIndexed { index, line ->
            val lineNum = index + 1
            val trimmed = line.trim()

            // Insecure dynamic execution: exec() or eval()
            if (trimmed.contains("exec(") || trimmed.contains("eval(")) {
                findings.add(
                    SecurityVulnerability(
                        id = "SEC-001",
                        cwe = "CWE-95",
                        title = "Dynamic Code Evaluation (exec/eval)",
                        description = "Executing dynamic Python code or evaluating string expressions can lead to Arbitrary Code Execution.",
                        line = lineNum,
                        severity = Severity.CRITICAL,
                        recommendation = "Avoid exec() and eval(). Parse inputs using structured serialization (JSON, YAML with SafeLoader)."
                    )
                )
            }

            // Insecure subprocess with shell=True
            if (trimmed.contains("shell=True") && (trimmed.contains("subprocess.") || trimmed.contains("Popen(") || trimmed.contains("run("))) {
                findings.add(
                    SecurityVulnerability(
                        id = "SEC-002",
                        cwe = "CWE-78",
                        title = "Command Injection Risk (shell=True)",
                        description = "Invoking system processes through a shell opens the application to Command Injection vulnerabilities.",
                        line = lineNum,
                        severity = Severity.HIGH,
                        recommendation = "Pass command arguments as an array/list with shell=False (default)."
                    )
                )
            }

            // Hardcoded credentials / secret tokens
            val secretPatterns = listOf(
                Regex("""(?i)(password|secret|api_key|token|auth_key)\s*=\s*['\"][a-zA-Z0-9_\-]{8,}['\"]"""),
                Regex("""ghp_[a-zA-Z0-9]{36}"""),
                Regex("""AIzaSy[a-zA-Z0-9_-]{33}""")
            )
            for (pattern in secretPatterns) {
                if (pattern.containsMatchIn(trimmed) && !trimmed.contains("os.getenv") && !trimmed.contains("os.environ")) {
                    findings.add(
                        SecurityVulnerability(
                            id = "SEC-003",
                            cwe = "CWE-798",
                            title = "Hardcoded Secret / API Token Pattern",
                            description = "Found hardcoded credential pattern in source code. Secrets should never be committed in plain text.",
                            line = lineNum,
                            severity = Severity.HIGH,
                            recommendation = "Load credentials from environment variables via os.environ.get('KEY_NAME') or a secure keystore."
                        )
                    )
                    break
                }
            }

            // Insecure pickle deserialization
            if (trimmed.contains("pickle.loads(") || trimmed.contains("pickle.load(")) {
                findings.add(
                    SecurityVulnerability(
                        id = "SEC-004",
                        cwe = "CWE-502",
                        title = "Unsafe Deserialization (pickle)",
                        description = "The pickle module is unsafe against untrusted inputs and can execute arbitrary bytecode upon deserialization.",
                        line = lineNum,
                        severity = Severity.CRITICAL,
                        recommendation = "Use safer alternatives like JSON, Protocol Buffers, or HMAC-signed payloads."
                    )
                )
            }

            // Hardcoded temporary directories
            if (trimmed.contains("'/tmp'") || trimmed.contains("\"/tmp\"") || trimmed.contains("'/tmp/")) {
                findings.add(
                    SecurityVulnerability(
                        id = "SEC-005",
                        cwe = "CWE-377",
                        title = "Hardcoded /tmp Directory Usage",
                        description = "Insecure usage of predictable temporary path. Predictable paths are susceptible to symlink race condition attacks.",
                        line = lineNum,
                        severity = Severity.LOW,
                        recommendation = "Use the standard library tempfile.NamedTemporaryFile() or tempfile.TemporaryDirectory()."
                    )
                )
            }

            // Insecure MD5/SHA1 cryptographic hashes
            if (trimmed.contains("hashlib.md5(") || trimmed.contains("hashlib.sha1(")) {
                findings.add(
                    SecurityVulnerability(
                        id = "SEC-006",
                        cwe = "CWE-328",
                        title = "Weak Cryptographic Hash (MD5/SHA1)",
                        description = "MD5 and SHA1 are cryptographically broken and vulnerable to hash collision attacks.",
                        line = lineNum,
                        severity = Severity.MEDIUM,
                        recommendation = "Use collision-resistant SHA-256 or SHA-512 via hashlib.sha256() instead."
                    )
                )
            }

            // Insecure HTTP URL detection
            if (trimmed.contains("http://") && !trimmed.contains("localhost") && !trimmed.contains("127.0.0.1") && !trimmed.contains("0.0.0.0")) {
                findings.add(
                    SecurityVulnerability(
                        id = "SEC-007",
                        cwe = "CWE-319",
                        title = "Cleartext Transmission of Sensitive Information (HTTP)",
                        description = "Using unencrypted HTTP for network communication exposes payloads to interception and man-in-the-middle attacks.",
                        line = lineNum,
                        severity = Severity.MEDIUM,
                        recommendation = "Always use encrypted HTTPS endpoints for external API requests."
                    )
                )
            }

            // Hardcoded JWT Token
            if (Regex("""eyJ[a-zA-Z0-9_-]{10,}\.[a-zA-Z0-9_-]{10,}\.[a-zA-Z0-9_-]{10,}""").containsMatchIn(trimmed)) {
                findings.add(
                    SecurityVulnerability(
                        id = "SEC-008",
                        cwe = "CWE-798",
                        title = "Hardcoded JSON Web Token (JWT)",
                        description = "Hardcoded JWT token found in source code. Hardcoded session tokens can be extracted and abused.",
                        line = lineNum,
                        severity = Severity.HIGH,
                        recommendation = "Generate or load JWT tokens dynamically via secure authentication flows."
                    )
                )
            }
        }

        // Calculate Security Score (0 to 100)
        var score = 100
        for (f in findings) {
            score -= when (f.severity) {
                Severity.CRITICAL -> 25
                Severity.HIGH -> 15
                Severity.MEDIUM -> 8
                Severity.LOW -> 3
                Severity.INFO -> 0
            }
        }
        score = score.coerceIn(0, 100)

        val stringBuilder = StringBuilder()
        stringBuilder.append("--------------------------------------------------------------------\n")
        stringBuilder.append("Security Pattern Scanner - Security Score: $score/100\n")
        stringBuilder.append("--------------------------------------------------------------------\n")
        if (findings.isEmpty()) {
            stringBuilder.append("✓ 0 security pattern risks detected. Clean pattern scan.\n")
        } else {
            stringBuilder.append("Detected ${findings.size} security patterns requiring review:\n\n")
            findings.forEach { v ->
                stringBuilder.append(">> [${v.id}: ${v.title}] Severity: ${v.severity} (${v.cwe})\n")
                stringBuilder.append("   Location: line ${v.line}\n")
                stringBuilder.append("   Details: ${v.description}\n")
                stringBuilder.append("   Remedy: ${v.recommendation}\n\n")
            }
        }

        return Pair(findings, stringBuilder.toString())
    }

    fun analyze(rawCode: String): QualityAnalysis {
        return try {
            val formatted = formatCode(rawCode)
            val (styleIssues, styleSummary) = analyzeStyle(formatted)
            val (typeIssues, typeSummary) = analyzeTypeAnnotations(formatted)
            val (securityFindings, secSummary) = scanSecurityPatterns(formatted)

            var secScore = 100
            for (v in securityFindings) {
                secScore -= when (v.severity) {
                    Severity.CRITICAL -> 25
                    Severity.HIGH -> 15
                    Severity.MEDIUM -> 8
                    Severity.LOW -> 3
                    Severity.INFO -> 0
                }
            }
            secScore = secScore.coerceIn(0, 100)

            QualityAnalysis(
                formattedCode = formatted,
                lintIssues = styleIssues,
                lintSummaryText = styleSummary,
                typeIssues = typeIssues,
                typeSummaryText = typeSummary,
                securityVulnerabilities = securityFindings,
                securitySummaryText = secSummary,
                securityScore = secScore,
                status = AnalysisStatus.SUCCESS
            )
        } catch (e: Throwable) {
            android.util.Log.e("CodeAnalysisEngine", "Error analyzing code", e)
            QualityAnalysis(
                formattedCode = rawCode,
                lintIssues = emptyList(),
                lintSummaryText = "Style Analysis: Analysis failed.",
                typeIssues = emptyList(),
                typeSummaryText = "Type Analysis: Static analysis failed.",
                securityVulnerabilities = emptyList(),
                securitySummaryText = "Security Pattern Scanner: Analysis failed (${e.message ?: "Unknown error"}). Score unavailable.",
                securityScore = -1,
                status = AnalysisStatus.FAILED,
                analysisErrors = listOf(e.message ?: "Unknown analysis failure")
            )
        }
    }
}
