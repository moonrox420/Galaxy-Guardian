package com.example.galaxyguardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.data.model.QualityAnalysis
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxySurfaceHighlight
import com.example.galaxyguardian.ui.theme.NeonGreen
import com.example.galaxyguardian.ui.theme.SecurityRed
import com.example.galaxyguardian.ui.theme.TerminalBackground
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.WarningAmber

@Composable
fun AuditResultsViewer(
    title: String,
    rawOutput: String,
    analysisType: String, // "lint", "typecheck", "security"
    analysis: QualityAnalysis?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GalaxySurfaceHighlight.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (badgeColor, badgeIcon) = when (analysisType) {
                        "security" -> if ((analysis?.securityScore ?: 100) >= 80) Pair(NeonGreen, Icons.Default.GppGood) else Pair(SecurityRed, Icons.Default.Warning)
                        "typecheck" -> Pair(CyanPrimary, Icons.Default.CheckCircle)
                        else -> Pair(WarningAmber, Icons.Default.CheckCircle)
                    }

                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = title,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                if (analysisType == "security" && analysis != null) {
                    val isFailed = analysis.status == com.example.galaxyguardian.data.model.AnalysisStatus.FAILED || analysis.securityScore < 0
                    val scoreColor = when {
                        isFailed -> SecurityRed
                        analysis.securityScore >= 85 -> NeonGreen
                        analysis.securityScore >= 60 -> WarningAmber
                        else -> SecurityRed
                    }
                    val scoreText = if (isFailed) "Security Score: FAILED" else "Security Score: ${analysis.securityScore}/100"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(scoreColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = scoreText,
                            color = scoreColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Summary metrics bar
            when (analysisType) {
                "security" -> {
                    val vulnCount = analysis?.securityVulnerabilities?.size ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeverityTag(
                            label = "$vulnCount Patterns Detected",
                            color = if (vulnCount == 0) NeonGreen else SecurityRed
                        )
                        SeverityTag(
                            label = "Engine: Security Pattern Scanner",
                            color = CyanPrimary
                        )
                    }
                }
                "lint" -> {
                    val issueCount = analysis?.lintIssues?.size ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeverityTag(
                            label = "$issueCount Style Notes",
                            color = if (issueCount == 0) NeonGreen else WarningAmber
                        )
                        SeverityTag(
                            label = "Standard: PEP 8 Style Checker",
                            color = CyanPrimary
                        )
                    }
                }
                "typecheck" -> {
                    val typeIssueCount = analysis?.typeIssues?.size ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeverityTag(
                            label = "$typeIssueCount Untyped Elements",
                            color = if (typeIssueCount == 0) NeonGreen else CyanPrimary
                        )
                        SeverityTag(
                            label = "Coverage: Type Annotation Checker",
                            color = CyanPrimary
                        )
                    }
                }
            }

            // Report content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalBackground)
                    .padding(12.dp)
                    .horizontalScroll(scrollState)
            ) {
                Text(
                    text = rawOutput.ifBlank { "No analysis data available. Run code generation to generate a full report." },
                    color = if (rawOutput.contains("Vulnerability") || rawOutput.contains("CRITICAL") || rawOutput.contains("Error")) WarningAmber else TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SeverityTag(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
