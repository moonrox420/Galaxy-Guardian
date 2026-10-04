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
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxySurfaceHighlight
import com.example.galaxyguardian.ui.theme.NeonGreen
import com.example.galaxyguardian.ui.theme.SecurityRed
import com.example.galaxyguardian.ui.theme.TerminalBackground
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextTertiary

sealed interface DiffLine {
    data class Unchanged(val text: String) : DiffLine
    data class Addition(val text: String) : DiffLine
    data class Deletion(val text: String) : DiffLine
}

object DiffCalculator {
    fun computeDiff(oldCode: String, newCode: String): List<DiffLine> {
        val oldLines = oldCode.lines()
        val newLines = newCode.lines()
        val diff = mutableListOf<DiffLine>()

        var i = 0
        var j = 0

        while (i < oldLines.size || j < newLines.size) {
            when {
                i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j] -> {
                    diff.add(DiffLine.Unchanged(oldLines[i]))
                    i++
                    j++
                }
                j < newLines.size && (i >= oldLines.size || !oldLines.contains(newLines[j])) -> {
                    diff.add(DiffLine.Addition(newLines[j]))
                    j++
                }
                i < oldLines.size -> {
                    diff.add(DiffLine.Deletion(oldLines[i]))
                    i++
                }
            }
        }
        return diff
    }
}

@Composable
fun DiffViewer(
    oldCode: String,
    newCode: String,
    title: String = "AI Repair Diff (Original vs Repaired)",
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val diffLines = DiffCalculator.computeDiff(oldCode, newCode)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GalaxySurfaceHighlight.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Compare,
                    contentDescription = null,
                    tint = CyanPrimary,
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalBackground)
                    .padding(12.dp)
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    diffLines.forEach { line ->
                        val (bgColor, textColor, prefix) = when (line) {
                            is DiffLine.Addition -> Triple(NeonGreen.copy(alpha = 0.15f), NeonGreen, "+ ")
                            is DiffLine.Deletion -> Triple(SecurityRed.copy(alpha = 0.15f), SecurityRed, "- ")
                            is DiffLine.Unchanged -> Triple(TerminalBackground, TextTertiary, "  ")
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(bgColor)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$prefix${when (line) {
                                    is DiffLine.Addition -> line.text
                                    is DiffLine.Deletion -> line.text
                                    is DiffLine.Unchanged -> line.text
                                }}",
                                color = textColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
