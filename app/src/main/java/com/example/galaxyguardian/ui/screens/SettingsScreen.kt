package com.example.galaxyguardian.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.data.repository.ThemeMode
import com.example.galaxyguardian.data.service.llm.LlmProviderType
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxyBackground
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxySurfaceHighlight
import com.example.galaxyguardian.ui.theme.NeonGreen
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextSecondary
import com.example.galaxyguardian.ui.theme.TextTertiary
import com.example.galaxyguardian.ui.viewmodel.GalaxyGuardianViewModel

@Composable
fun SettingsScreen(
    viewModel: GalaxyGuardianViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val availableOllamaModels by viewModel.availableOllamaModels.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val modelChipScrollState = rememberScrollState()

    var tempApiKey by remember(uiState.customApiKey) { mutableStateOf(uiState.customApiKey) }
    var tempOllamaUrl by remember(uiState.ollamaBaseUrl) { mutableStateOf(uiState.ollamaBaseUrl) }
    var tempOllamaModel by remember(uiState.ollamaModelName) { mutableStateOf(uiState.ollamaModelName) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 80.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Configuration & Model",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize AI providers, Ollama endpoints, and simulation policies",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // LLM Generation Provider Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generation Engine Provider",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select the engine responsible for synthesizing code and architecture.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val providers = listOf(
                        Triple(
                            LlmProviderType.GEMINI,
                            "Google Gemini (Cloud)",
                            "High-performance cloud inference via Gemini REST API."
                        ),
                        Triple(
                            LlmProviderType.OLLAMA,
                            "Ollama (Local / Self-Hosted)",
                            "Private inference via local desktop or LAN Ollama server."
                        ),
                        Triple(
                            LlmProviderType.OFFLINE_ONLY,
                            "Offline Template Baseline",
                            "Zero-network, category-aware bot synthesizer."
                        )
                    )

                    providers.forEach { (provider, title, desc) ->
                        val isSelected = uiState.providerType == provider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setProviderType(provider) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setProviderType(provider) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CyanPrimary,
                                    unselectedColor = GalaxySurfaceHighlight
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = title,
                                    color = TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = desc,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Provider-Specific Configuration Card
            when (uiState.providerType) {
                LlmProviderType.GEMINI -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini Configuration",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Model Selection:",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            val geminiModels = listOf(
                                Pair("gemini-3.5-flash", "Gemini 3.5 Flash (Fast, high-throughput code synthesis)"),
                                Pair("gemini-3.1-pro-preview", "Gemini 3.1 Pro (Deep architectural reasoning)")
                            )

                            geminiModels.forEach { (modelId, desc) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = uiState.selectedModel == modelId,
                                        onClick = { viewModel.setModel(modelId) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = CyanPrimary,
                                            unselectedColor = GalaxySurfaceHighlight
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = modelId, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = desc, color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Custom API Key (Encrypted in Keystore):",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = tempApiKey,
                                onValueChange = { tempApiKey = it },
                                placeholder = { Text("Paste custom Gemini API key...", color = TextTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = GalaxyBackground,
                                    unfocusedContainerColor = GalaxyBackground,
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = GalaxySurfaceHighlight,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    viewModel.setCustomApiKey(tempApiKey.trim())
                                    Toast.makeText(context, "API Key securely saved", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanPrimary,
                                    contentColor = GalaxyBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Save Key", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                LlmProviderType.OLLAMA -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ollama Server Settings",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Base URL (Emulator: http://10.0.2.2:11434, USB: http://localhost:11434 via adb reverse):",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = tempOllamaUrl,
                                onValueChange = { tempOllamaUrl = it },
                                placeholder = { Text("http://10.0.2.2:11434", color = TextTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = GalaxyBackground,
                                    unfocusedContainerColor = GalaxyBackground,
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = GalaxySurfaceHighlight,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Model:",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )

                                OutlinedButton(
                                    onClick = { viewModel.fetchOllamaModels() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Discover Models", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (availableOllamaModels.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(modelChipScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableOllamaModels.forEach { modelName ->
                                        val isSelected = tempOllamaModel == modelName
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                tempOllamaModel = modelName
                                                viewModel.setOllamaModelName(modelName)
                                            },
                                            label = { Text(modelName, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = NeonGreen.copy(alpha = 0.25f),
                                                selectedLabelColor = NeonGreen,
                                                containerColor = GalaxySurfaceHighlight,
                                                labelColor = TextSecondary
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            OutlinedTextField(
                                value = tempOllamaModel,
                                onValueChange = { tempOllamaModel = it },
                                placeholder = { Text("qwen2.5-coder:7b", color = TextTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = GalaxyBackground,
                                    unfocusedContainerColor = GalaxyBackground,
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = GalaxySurfaceHighlight,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    viewModel.setOllamaBaseUrl(tempOllamaUrl.trim())
                                    viewModel.setOllamaModelName(tempOllamaModel.trim())
                                    Toast.makeText(context, "Ollama settings saved", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanPrimary,
                                    contentColor = GalaxyBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Save Ollama Config", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                LlmProviderType.OFFLINE_ONLY -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Offline Baseline Mode Active",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No external network connections or API tokens required. Galaxy Guardian synthesizes complete, category-tailored code architectures using deterministic local templates and persona directives.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Theme & Appearance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Appearance & Theme",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Switch between dark and light themes system-wide",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val themeOptions = listOf(
                        Triple(ThemeMode.SYSTEM, "System Default", "Follows device appearance setting"),
                        Triple(ThemeMode.DARK, "Dark Galaxy", "Deep space contrast with glowing cyber accents"),
                        Triple(ThemeMode.LIGHT, "Light Solar", "Crisp high-readability daylight theme")
                    )

                    themeOptions.forEach { (mode, title, desc) ->
                        val isSelected = themeMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    Toast.makeText(context, "Theme set to: $title", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    Toast.makeText(context, "Theme set to: $title", Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CyanPrimary,
                                    unselectedColor = GalaxySurfaceHighlight
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = title,
                                    color = TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = desc,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Diagnostics & Export Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Diagnostics & Export",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Export sanitized diagnostics logs for debugging without exposing secrets.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val diagnosticsText = viewModel.exportDiagnostics()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, diagnosticsText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Diagnostics Log"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Sanitized Diagnostics Log")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Galaxy Guardian vNext",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Android AI Builder & Autonomous Workbench",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
