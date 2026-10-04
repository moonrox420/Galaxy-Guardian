package com.example.galaxyguardian.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.data.model.ArchitectureProfile
import com.example.galaxyguardian.data.model.GenerationTarget
import com.example.galaxyguardian.data.model.TargetLanguage
import com.example.galaxyguardian.data.service.llm.LlmProviderType
import com.example.galaxyguardian.ui.components.AuditResultsViewer
import com.example.galaxyguardian.ui.components.BotHeaderBanner
import com.example.galaxyguardian.ui.components.CodeViewer
import com.example.galaxyguardian.ui.components.ConsoleTerminal
import com.example.galaxyguardian.ui.components.DiffViewer
import com.example.galaxyguardian.ui.theme.CosmicPurple
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxyBackground
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxySurfaceHighlight
import com.example.galaxyguardian.ui.theme.NeonGreen
import com.example.galaxyguardian.ui.theme.SecurityRed
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextSecondary
import com.example.galaxyguardian.ui.viewmodel.GalaxyGuardianViewModel
import com.example.galaxyguardian.ui.viewmodel.ResultsTab

@Composable
fun HomeScreen(
    viewModel: GalaxyGuardianViewModel,
    onNavigateToPersonality: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val chipScrollState = rememberScrollState()
    val targetScrollState = rememberScrollState()

    val zipExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.exportProjectZip(context, uri)
        }
    }

    val quickIdeas = if (uiState.targetLanguage == TargetLanguage.KOTLIN_ANDROID) {
        listOf(
            "a habit tracker with local Room storage",
            "a weather dashboard with Jetpack Compose",
            "a crypto portfolio tracker screen",
            "a notes feature with ViewModel and StateFlow",
            "a WorkManager background sync worker"
        )
    } else {
        listOf(
            "a bot that fetches weather data",
            "a discord auto-moderation bot",
            "a crypto price tracker bot",
            "a github bot that triages incoming issues",
            "a web scraper bot for tech headlines"
        )
    }

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
                .padding(bottom = 80.dp)
        ) {
            // Hero banner
            BotHeaderBanner(securityScore = uiState.analysis?.securityScore)

            // Prompt Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Language Selector Row
                    Text(
                        text = "Target Platform & Language:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TargetLanguage.entries.forEach { lang ->
                            val isSelected = uiState.targetLanguage == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setTargetLanguage(lang) },
                                label = {
                                    Text(
                                        text = lang.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = CyanPrimary,
                                    containerColor = GalaxySurfaceHighlight,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Generation Target Component Selector Row
                    Text(
                        text = "Target Component:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(targetScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.targetLanguage.supportedTargets().forEach { target ->
                            val isSelected = uiState.generationTarget == target
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setGenerationTarget(target) },
                                label = { Text(text = target.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = NeonGreen,
                                    containerColor = GalaxySurfaceHighlight.copy(alpha = 0.5f),
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    if (uiState.targetLanguage == TargetLanguage.KOTLIN_ANDROID) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ArchitectureProfile.entries.forEach { arch ->
                                val isSelected = uiState.architectureProfile == arch
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setArchitectureProfile(arch) },
                                    label = { Text(text = arch.displayName, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CosmicPurple.copy(alpha = 0.25f),
                                        selectedLabelColor = CosmicPurple,
                                        containerColor = GalaxySurfaceHighlight.copy(alpha = 0.3f),
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (uiState.targetLanguage == TargetLanguage.KOTLIN_ANDROID) "What do you want to build?" else "Enter your idea for the bot:",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Active Persona & Active Provider Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Persona Pill
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicPurple.copy(alpha = 0.15f))
                                .clickable { onNavigateToPersonality() }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = CosmicPurple,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.personality.name,
                                    color = CosmicPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "›",
                                color = CosmicPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Provider Pill
                        val (providerColor, providerLabel) = when (uiState.providerType) {
                            LlmProviderType.GEMINI -> CyanPrimary to "Gemini (${uiState.selectedModel.removePrefix("gemini-")})"
                            LlmProviderType.OLLAMA -> NeonGreen to "Ollama (${uiState.ollamaModelName})"
                            LlmProviderType.OFFLINE_ONLY -> CosmicPurple to "Offline Templates"
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(providerColor.copy(alpha = 0.15f))
                                .clickable { onNavigateToSettings() }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = providerColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = providerLabel,
                                    color = providerColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "›",
                                color = providerColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.prompt,
                        onValueChange = { viewModel.onPromptChange(it) },
                        placeholder = {
                            Text(
                                if (uiState.targetLanguage == TargetLanguage.KOTLIN_ANDROID) "e.g., a habit tracker with local Room storage" else "e.g., a bot that fetches weather data",
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GalaxyBackground,
                            unfocusedContainerColor = GalaxyBackground,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = GalaxySurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = false,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick suggestion chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chipScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickIdeas.forEach { idea ->
                            FilterChip(
                                selected = uiState.prompt == idea,
                                onClick = { viewModel.onPromptChange(idea) },
                                label = {
                                    Text(
                                        text = idea.removePrefix("a ").capitalizeIdea(),
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = GalaxySurfaceHighlight.copy(alpha = 0.5f),
                                    labelColor = TextSecondary,
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = GalaxySurfaceHighlight,
                                    selectedBorderColor = CyanPrimary,
                                    enabled = true,
                                    selected = uiState.prompt == idea
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.generateCode() },
                            enabled = !uiState.isGenerating && !uiState.isExecuting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = GalaxyBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("generate_code_button")
                        ) {
                            if (uiState.isGenerating) {
                                CircularProgressIndicator(
                                    color = GalaxyBackground,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Generate",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Code", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.executeCode() },
                            enabled = !uiState.isGenerating && !uiState.isExecuting && (uiState.generatedCode.isNotBlank() || uiState.formattedCode.isNotBlank()),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = GalaxyBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("quick_execute_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run", fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { viewModel.saveCurrentBot() },
                            enabled = uiState.generatedCode.isNotBlank() || uiState.formattedCode.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GalaxySurfaceHighlight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save Project",
                                tint = CyanPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                val sanitizeTitle = uiState.prompt.take(20).replace(" ", "_")
                                zipExportLauncher.launch("${sanitizeTitle}_project.zip")
                            },
                            enabled = uiState.generatedCode.isNotBlank() || uiState.formattedCode.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GalaxySurfaceHighlight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export ZIP",
                                tint = NeonGreen
                            )
                        }

                        IconButton(
                            onClick = {
                                val codeToShare = uiState.formattedCode.ifBlank { uiState.generatedCode }
                                if (codeToShare.isNotBlank()) {
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, codeToShare)
                                        type = "text/plain"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Project Source via")
                                    context.startActivity(shareIntent)
                                }
                            },
                            enabled = uiState.generatedCode.isNotBlank() || uiState.formattedCode.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GalaxySurfaceHighlight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Code",
                                tint = TextSecondary
                            )
                        }
                    }

                    // Action tools row: Self-Repair & Generate Tests
                    if (uiState.generatedCode.isNotBlank() || uiState.formattedCode.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.improveAndFixCode() },
                                enabled = !uiState.isGenerating && !uiState.isExecuting,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Improve & Fix", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.generateTests() },
                                enabled = !uiState.isGenerating && !uiState.isExecuting,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Tests", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Status or Error notification banner
            if (uiState.statusMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyanPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = uiState.statusMessage ?: "",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SecurityRed.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = SecurityRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = uiState.currentTab.ordinal,
                containerColor = GalaxySurface,
                contentColor = CyanPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.currentTab.ordinal]),
                        color = CyanPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Tab(
                    selected = uiState.currentTab == ResultsTab.GENERATED_CODE,
                    onClick = { viewModel.setTab(ResultsTab.GENERATED_CODE) },
                    text = { Text("Generated Source", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = uiState.currentTab == ResultsTab.FORMATTED_CODE,
                    onClick = { viewModel.setTab(ResultsTab.FORMATTED_CODE) },
                    text = { Text("Formatted Source", fontSize = 13.sp) },
                    icon = { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = uiState.currentTab == ResultsTab.DIFF_VIEW,
                    onClick = { viewModel.setTab(ResultsTab.DIFF_VIEW) },
                    text = { Text("Diff View", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = uiState.currentTab == ResultsTab.LINT_RESULTS,
                    onClick = { viewModel.setTab(ResultsTab.LINT_RESULTS) },
                    text = { Text("Style & Lint", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = uiState.currentTab == ResultsTab.TYPE_CHECK,
                    onClick = { viewModel.setTab(ResultsTab.TYPE_CHECK) },
                    text = { Text("Type Checker", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = uiState.currentTab == ResultsTab.SECURITY_AUDIT,
                    onClick = { viewModel.setTab(ResultsTab.SECURITY_AUDIT) },
                    text = { Text("Security Scan", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = uiState.currentTab == ResultsTab.SIMULATION,
                    onClick = { viewModel.setTab(ResultsTab.SIMULATION) },
                    text = { Text("Lifecycle Sim", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Active Tab Content View
            when (uiState.currentTab) {
                ResultsTab.GENERATED_CODE -> {
                    CodeViewer(
                        code = uiState.generatedCode,
                        title = "Generated Source [${uiState.targetLanguage.displayName}]"
                    )
                }
                ResultsTab.FORMATTED_CODE -> {
                    CodeViewer(
                        code = uiState.formattedCode,
                        title = "Formatted Source"
                    )
                }
                ResultsTab.DIFF_VIEW -> {
                    DiffViewer(
                        oldCode = uiState.originalCodeForDiff,
                        newCode = uiState.formattedCode.ifBlank { uiState.generatedCode },
                        title = "Diff (Original vs Repaired / Updated)"
                    )
                }
                ResultsTab.LINT_RESULTS -> {
                    AuditResultsViewer(
                        title = "Code Style Analysis",
                        rawOutput = uiState.analysis?.lintSummaryText ?: "",
                        analysisType = "lint",
                        analysis = uiState.analysis
                    )
                }
                ResultsTab.TYPE_CHECK -> {
                    AuditResultsViewer(
                        title = "Type Checking Analysis",
                        rawOutput = uiState.analysis?.typeSummaryText ?: "",
                        analysisType = "typecheck",
                        analysis = uiState.analysis
                    )
                }
                ResultsTab.SECURITY_AUDIT -> {
                    AuditResultsViewer(
                        title = "Security Pattern Analysis",
                        rawOutput = uiState.analysis?.securitySummaryText ?: "",
                        analysisType = "security",
                        analysis = uiState.analysis
                    )
                }
                ResultsTab.SIMULATION -> {
                    ConsoleTerminal(
                        executionResult = uiState.executionResult,
                        isExecuting = uiState.isExecuting,
                        onExecuteClick = { viewModel.executeCode() },
                        selectedScenario = uiState.selectedScenario,
                        onScenarioSelected = { viewModel.setSimulationScenario(it) }
                    )
                }
            }
        }
    }
}

private fun String.capitalizeIdea(): String =
    if (isNotEmpty()) substring(0, 1).uppercase() + substring(1) else this
