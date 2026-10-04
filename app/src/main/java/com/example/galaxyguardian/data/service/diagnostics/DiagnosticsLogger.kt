package com.example.galaxyguardian.data.service.diagnostics

import com.example.galaxyguardian.data.model.QualityAnalysis
import com.example.galaxyguardian.data.model.TargetLanguage
import com.example.galaxyguardian.data.service.llm.LlmProviderType

data class GenerationMetric(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: LlmProviderType,
    val model: String,
    val language: TargetLanguage,
    val latencyMs: Long,
    val securityScore: Int,
    val isSuccess: Boolean
)

object DiagnosticsLogger {

    private val metricsList = mutableListOf<GenerationMetric>()

    fun logMetric(metric: GenerationMetric) {
        synchronized(metricsList) {
            if (metricsList.size >= 100) {
                metricsList.removeAt(0)
            }
            metricsList.add(metric)
        }
    }

    fun exportSanitizedDiagnostics(analysis: QualityAnalysis?): String {
        val sb = StringBuilder()
        sb.append("=== GALAXY GUARDIAN SANITIZED DIAGNOSTICS REPORT ===\n")
        sb.append("Timestamp: ${System.currentTimeMillis()}\n")
        sb.append("Total Executed Generations: ${metricsList.size}\n\n")

        if (analysis != null) {
            sb.append("Active Analysis Security Score: ${analysis.securityScore}/100\n")
            sb.append("Security Findings Count: ${analysis.securityVulnerabilities.size}\n")
            sb.append("Style Notes Count: ${analysis.lintIssues.size}\n")
            sb.append("Type Suggestions Count: ${analysis.typeIssues.size}\n\n")
        }

        sb.append("--- Generation Metrics History ---\n")
        synchronized(metricsList) {
            metricsList.forEach { m ->
                sb.append("[${m.timestamp}] Provider: ${m.provider} | Model: ${m.model} | Lang: ${m.language.displayName} | Latency: ${m.latencyMs}ms | Score: ${m.securityScore}\n")
            }
        }
        sb.append("=== END DIAGNOSTICS REPORT ===\n")

        return sanitizeSecrets(sb.toString())
    }

    private fun sanitizeSecrets(input: String): String {
        return input
            .replace(Regex("""AIzaSy[a-zA-Z0-9_-]{33}"""), "AIzaSy[REDACTED_API_KEY]")
            .replace(Regex("""ghp_[a-zA-Z0-9]{36}"""), "ghp_[REDACTED_GITHUB_TOKEN]")
            .replace(Regex("""sk-[a-zA-Z0-9]{32,}"""), "sk-[REDACTED_SECRET]")
    }
}
