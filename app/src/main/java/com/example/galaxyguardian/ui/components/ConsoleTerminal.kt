package com.example.galaxyguardian.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.data.model.ExecutionResult
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxySurfaceHighlight
import com.example.galaxyguardian.ui.theme.NeonGreen
import com.example.galaxyguardian.ui.theme.SecurityRed
import com.example.galaxyguardian.ui.theme.TerminalBackground
import com.example.galaxyguardian.ui.theme.TerminalText
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextSecondary
import com.example.galaxyguardian.ui.theme.TextTertiary

@Composable
fun ConsoleTerminal(
    executionResult: ExecutionResult?,
    isExecuting: Boolean,
    onExecuteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GalaxySurfaceHighlight.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Terminal",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lifecycle Simulation Console",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (executionResult != null) {
                        val statusColor = if (executionResult.isSuccess) NeonGreen else SecurityRed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Exit ${executionResult.exitCode} (${executionResult.durationMs}ms)",
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                val fullOutput = "--- Simulation Output ---\n${executionResult.stdout}\n--- Errors ---\n${executionResult.stderr}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Simulation Output", fullOutput)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Terminal output copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy output",
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Action Execute button bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode: In-Memory Lifecycle Simulator (Safe on-device simulation)",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onExecuteClick,
                    enabled = !isExecuting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = GalaxySurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("execute_code_button")
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            color = GalaxySurface,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulating...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Simulate",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Run", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Terminal text viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalBackground)
                    .padding(14.dp)
                    .horizontalScroll(scrollState)
            ) {
                if (isExecuting) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = NeonGreen,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Initializing safe bot lifecycle simulator...",
                            color = TerminalText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                } else if (executionResult == null) {
                    Text(
                        text = "--- Simulation Output ---\n(Press 'Simulate Run' to test bot event loop in memory)\n--- Errors ---\n(None)",
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                } else {
                    Column {
                        Text(
                            text = "--- Simulation Output ---",
                            color = CyanPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = executionResult.stdout.ifBlank { "(No standard output emitted)\n" },
                            color = TerminalText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "--- Errors ---",
                            color = if (executionResult.stderr.isNotBlank()) SecurityRed else TextTertiary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = executionResult.stderr.ifBlank { "(None)\n" },
                            color = if (executionResult.stderr.isNotBlank()) SecurityRed else TextTertiary,
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
