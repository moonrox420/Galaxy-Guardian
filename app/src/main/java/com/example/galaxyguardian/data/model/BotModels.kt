package com.example.galaxyguardian.data.model

enum class Severity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class LintIssue(
    val line: Int,
    val ruleCode: String,
    val message: String,
    val severity: Severity = Severity.LOW
)

data class TypeIssue(
    val line: Int,
    val symbol: String,
    val message: String,
    val severity: Severity = Severity.MEDIUM
)

enum class AnalysisStatus {
    SUCCESS,
    PARTIAL,
    FAILED
}

data class SecurityVulnerability(
    val id: String,
    val cwe: String,
    val title: String,
    val description: String,
    val line: Int,
    val severity: Severity,
    val recommendation: String
)

data class QualityAnalysis(
    val formattedCode: String,
    val lintIssues: List<LintIssue>,
    val lintSummaryText: String,
    val typeIssues: List<TypeIssue>,
    val typeSummaryText: String,
    val securityVulnerabilities: List<SecurityVulnerability>,
    val securitySummaryText: String,
    val securityScore: Int, // 0 to 100, or -1 if FAILED
    val status: AnalysisStatus = AnalysisStatus.SUCCESS,
    val analysisErrors: List<String> = emptyList()
)

data class ExecutionResult(
    val id: String,
    val timestamp: Long,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long,
    val isSuccess: Boolean
)

data class BotProject(
    val id: String,
    val title: String,
    val prompt: String,
    val targetLanguage: String = "python",
    val generatedCode: String,
    val formattedCode: String,
    val analysis: QualityAnalysis,
    val lastExecution: ExecutionResult? = null,
    val providerType: String = "GEMINI",
    val modelName: String = "gemini-3.5-flash",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val generationTarget: String = "AUTO",
    val architectureProfile: String = "MVVM",
    val filesJson: String = "[]",
    val versionNumber: Int = 1
) {
    fun toTargetLanguage(): TargetLanguage = TargetLanguage.fromPersistenceId(targetLanguage)
}

data class BotTemplate(
    val id: String,
    val title: String,
    val description: String,
    val prompt: String,
    val iconName: String,
    val sampleCode: String
)

data class BotPersonality(
    val id: String = "default_persona",
    val name: String = "Galaxy Guardian Alpha",
    val tone: String = "Vigilant, protective, and mission-critical",
    val traits: String = "Analytical, defensive, highly disciplined, security-conscious",
    val systemInstructions: String = "Always validate all inputs before processing. Never output plain text secrets or passwords. Write clean PEP 8 code with comprehensive docstrings and type annotations. Log warnings for anomalous events.",
    val negativeConstraints: String = "Do NOT use eval(), exec(), shell=True, or wildcard imports. Do NOT leave untyped functions.",
    val responseStyle: String = "Modular enterprise Python with clear terminal feedback",
    val creativityTemperature: Float = 0.3f,
    val avatarType: String = "icon", // "icon" or "procedural"
    val avatarValue: String = "shield", // icon key or procedural seed name
    val avatarColorIndex: Int = 0 // color theme index for avatar glow
)
