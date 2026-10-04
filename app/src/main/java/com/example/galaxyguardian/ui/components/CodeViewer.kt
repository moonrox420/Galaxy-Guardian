package com.example.galaxyguardian.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.data.service.llm.MultiFileProjectParser
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxySurfaceHighlight
import com.example.galaxyguardian.ui.theme.TerminalBackground
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextSecondary
import com.example.galaxyguardian.ui.theme.TextTertiary

@Composable
fun CodeViewer(
    code: String,
    title: String,
    modifier: Modifier = Modifier,
    emptyPlaceholder: String = "No code generated yet. Enter a prompt above and tap 'Generate Code'."
) {
    val context = LocalContext.current
    val horizontalScrollState = rememberScrollState()
    val fileChipScrollState = rememberScrollState()

    val parsedFiles = remember(code) { MultiFileProjectParser.parse(code) }
    var selectedFileIndex by remember(code) { mutableStateOf(0) }

    val activeFile = parsedFiles.getOrNull(selectedFileIndex) ?: parsedFiles.firstOrNull()
    val activeCode = activeFile?.content ?: code
    val activePath = activeFile?.path ?: title

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
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(CyanPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    if (activeCode.isNotBlank()) {
                        val linesCount = activeCode.lines().size
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "($linesCount lines)",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (activeCode.isNotBlank()) {
                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Galaxy Guardian Code", activeCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, activeCode)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Code"))
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share code",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Multi-file Project File Selector Tabs
            if (parsedFiles.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GalaxySurfaceHighlight.copy(alpha = 0.3f))
                        .horizontalScroll(fileChipScrollState)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsedFiles.forEachIndexed { idx, file ->
                        val isSelected = idx == selectedFileIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFileIndex = idx },
                            label = {
                                Text(
                                    text = file.path,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = CyanPrimary,
                                containerColor = GalaxySurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Code content or placeholder
            if (activeCode.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyPlaceholder,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            } else {
                val lines = activeCode.lines()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalBackground)
                        .padding(12.dp)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    // Line numbers column
                    Column(modifier = Modifier.padding(end = 12.dp)) {
                        lines.indices.forEach { index ->
                            Text(
                                text = "${index + 1}",
                                color = TextTertiary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Code text column
                    Column {
                        lines.forEach { line ->
                            Text(
                                text = line.ifEmpty { " " },
                                color = TextPrimary,
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
