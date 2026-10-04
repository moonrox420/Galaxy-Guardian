package com.example.galaxyguardian.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.galaxyguardian.data.model.BotPersonality
import com.example.galaxyguardian.ui.components.BotAvatarPickerComponent
import com.example.galaxyguardian.ui.components.BotAvatarView
import com.example.galaxyguardian.ui.theme.CosmicPurple
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxyBackground
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxySurfaceHighlight
import com.example.galaxyguardian.ui.theme.NeonGreen
import com.example.galaxyguardian.ui.theme.SecurityRed
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextSecondary
import com.example.galaxyguardian.ui.theme.TextTertiary
import com.example.galaxyguardian.ui.theme.WarningAmber
import com.example.galaxyguardian.ui.viewmodel.GalaxyGuardianViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalityScreen(
    viewModel: GalaxyGuardianViewModel,
    onNavigateToForge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val presetChipScrollState = rememberScrollState()
    val traitChipScrollState = rememberScrollState()

    // Local editing state initialized from ViewModel's active persona
    var name by remember(uiState.personality.id) { mutableStateOf(uiState.personality.name) }
    var tone by remember(uiState.personality.id) { mutableStateOf(uiState.personality.tone) }
    var traits by remember(uiState.personality.id) { mutableStateOf(uiState.personality.traits) }
    var systemInstructions by remember(uiState.personality.id) { mutableStateOf(uiState.personality.systemInstructions) }
    var negativeConstraints by remember(uiState.personality.id) { mutableStateOf(uiState.personality.negativeConstraints) }
    var responseStyle by remember(uiState.personality.id) { mutableStateOf(uiState.personality.responseStyle) }
    var creativityTemperature by remember(uiState.personality.id) { mutableFloatStateOf(uiState.personality.creativityTemperature) }
    var avatarType by remember(uiState.personality.id) { mutableStateOf(uiState.personality.avatarType) }
    var avatarValue by remember(uiState.personality.id) { mutableStateOf(uiState.personality.avatarValue) }
    var avatarColorIndex by remember(uiState.personality.id) { mutableIntStateOf(uiState.personality.avatarColorIndex) }

    // Validation Tracking State
    var hasAttemptedSave by remember { mutableStateOf(false) }

    // Dialog Visibility States
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val isNameEmpty = name.trim().isBlank()
    val isTraitsEmpty = traits.trim().isBlank()
    val isInstructionsEmpty = systemInstructions.trim().isBlank()
    val hasValidationError = isNameEmpty || isTraitsEmpty || isInstructionsEmpty

    // Active Personality Snapshot for Export
    val currentPersonalitySnapshot = BotPersonality(
        id = uiState.personality.id,
        name = name,
        tone = tone,
        traits = traits,
        systemInstructions = systemInstructions,
        negativeConstraints = negativeConstraints,
        responseStyle = responseStyle,
        creativityTemperature = creativityTemperature,
        avatarType = avatarType,
        avatarValue = avatarValue,
        avatarColorIndex = avatarColorIndex
    )

    // Export to File SAF Launcher (Zero Permissions Required)
    val exportJsonString = remember(currentPersonalitySnapshot) {
        viewModel.exportPersonalityAsJson(currentPersonalitySnapshot)
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(exportJsonString.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Saved configuration to file!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun performValidatedSave(onSuccess: () -> Unit) {
        hasAttemptedSave = true
        if (hasValidationError) {
            val errorMsg = when {
                isTraitsEmpty && isInstructionsEmpty -> "Personality traits and system instructions cannot be empty!"
                isTraitsEmpty -> "Personality traits cannot be empty! Please define at least one trait."
                isInstructionsEmpty -> "System instructions cannot be empty! Directives are required for bot code synthesis."
                else -> "Bot designation / name cannot be empty!"
            }
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            return
        }

        // Inputs are valid: persist to DataStore via ViewModel
        val success = viewModel.updatePersonality(
            name = name,
            tone = tone,
            traits = traits,
            systemInstructions = systemInstructions,
            negativeConstraints = negativeConstraints,
            responseStyle = responseStyle,
            creativityTemperature = creativityTemperature,
            avatarType = avatarType,
            avatarValue = avatarValue,
            avatarColorIndex = avatarColorIndex
        )

        if (success) {
            onSuccess()
        }
    }

    val quickAddTraits = listOf(
        "Security-First",
        "Analytical",
        "Cyberpunk",
        "Witty",
        "Sarcastic",
        "Polite & Formal",
        "Educational",
        "Autonomous",
        "Minimalist"
    )

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
            // Screen Header & Top Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CosmicPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = CosmicPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bot Persona",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DataStore Active",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Traits, Directives & Config",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Top Utility Actions: Export JSON & Reset Defaults
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Export JSON button
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyanPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("export_persona_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export JSON",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Reset to Defaults button
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SecurityRed
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("reset_to_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset to Defaults",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real-Time Validation Warning Alert Banner
            AnimatedVisibility(
                visible = hasAttemptedSave && hasValidationError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("validation_error_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SecurityRed.copy(alpha = 0.18f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SecurityRed)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Validation Error",
                            tint = SecurityRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Validation Warning: Incomplete Directives",
                                color = SecurityRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Personality traits and system instructions cannot be empty before saving to DataStore.",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // Predefined Personality Templates Section
            // Allows users to quickly populate the personality and instruction fields
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Predefined Personality Templates:",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to auto-fill",
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(presetChipScrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                viewModel.personalityPresets.forEach { preset ->
                    val isSelected = uiState.personality.id == preset.id || name == preset.name
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.applyPersonalityPreset(preset)
                                name = preset.name
                                tone = preset.tone
                                traits = preset.traits
                                systemInstructions = preset.systemInstructions
                                negativeConstraints = preset.negativeConstraints
                                responseStyle = preset.responseStyle
                                creativityTemperature = preset.creativityTemperature
                                avatarType = preset.avatarType
                                avatarValue = preset.avatarValue
                                avatarColorIndex = preset.avatarColorIndex
                                hasAttemptedSave = false
                                Toast.makeText(context, "Populated template: '${preset.name}'", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("personality_template_${preset.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GalaxySurfaceHighlight else GalaxySurface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) CyanPrimary else GalaxySurfaceHighlight
                            )
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BotAvatarView(
                                    avatarType = preset.avatarType,
                                    avatarValue = preset.avatarValue,
                                    colorIndex = preset.avatarColorIndex,
                                    size = 36.dp
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyanPrimary.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            color = CyanPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = preset.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )

                            Text(
                                text = preset.tone,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val firstTrait = preset.traits.split(",").firstOrNull()?.trim() ?: "Specialized"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CosmicPurple.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = firstTrait,
                                    color = CosmicPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Component 1: Avatar Selector & Generator
            BotAvatarPickerComponent(
                currentType = avatarType,
                currentValue = avatarValue,
                currentColorIndex = avatarColorIndex,
                onAvatarChanged = { type, value, colorIdx ->
                    avatarType = type
                    avatarValue = value
                    avatarColorIndex = colorIdx
                    viewModel.updateAvatar(type, value, colorIdx)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Component 2: REAL-TIME LIVE PREVIEW COMPONENT
            BotPersonalityPreviewSummaryCard(
                name = name,
                tone = tone,
                traits = traits,
                systemInstructions = systemInstructions,
                negativeConstraints = negativeConstraints,
                responseStyle = responseStyle,
                creativityTemperature = creativityTemperature,
                avatarType = avatarType,
                avatarValue = avatarValue,
                avatarColorIndex = avatarColorIndex
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main Configuration Card with Text Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Field 1: Bot Designation / Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bot Designation / Name *",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isNameEmpty && hasAttemptedSave) {
                            Text("Required", color = SecurityRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        isError = hasAttemptedSave && isNameEmpty,
                        supportingText = {
                            if (hasAttemptedSave && isNameEmpty) {
                                Text("Bot designation / name cannot be empty", color = SecurityRed, fontSize = 11.sp)
                            }
                        },
                        placeholder = { Text("e.g., Galaxy Guardian Alpha", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bot_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedFieldColors(isError = hasAttemptedSave && isNameEmpty),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 2: Tone & Demeanor
                    Text(
                        text = "Tone & Demeanor",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "How the bot communicates and approaches operations",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tone,
                        onValueChange = { tone = it },
                        placeholder = { Text("e.g., Vigilant, protective, mission-critical", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bot_tone_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 3: Personality Traits (VALIDATED)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Personality Traits *",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isTraitsEmpty) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (hasAttemptedSave) SecurityRed.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (hasAttemptedSave) "Required" else "Cannot be empty",
                                        color = if (hasAttemptedSave) SecurityRed else WarningAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Comma-separated",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = traits,
                        onValueChange = { traits = it },
                        isError = hasAttemptedSave && isTraitsEmpty,
                        supportingText = {
                            if (hasAttemptedSave && isTraitsEmpty) {
                                Text(
                                    text = "Personality traits cannot be empty (e.g., Security-First, Analytical)",
                                    color = SecurityRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "Updates real-time preview above automatically as you type",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        placeholder = { Text("e.g., Security-first, analytical, zero-trust mindset, disciplined", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bot_traits_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedFieldColors(isError = hasAttemptedSave && isTraitsEmpty),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick trait add chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(traitChipScrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickAddTraits.forEach { trait ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GalaxySurfaceHighlight)
                                    .border(1.dp, GalaxySurfaceHighlight, RoundedCornerShape(8.dp))
                                    .clickable {
                                        traits = if (traits.trim().isBlank()) trait else "${traits.trim()}, $trait"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+ $trait",
                                    color = CyanPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 4: System Instructions / Directives (VALIDATED)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "System Instructions (Directives) *",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isInstructionsEmpty) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (hasAttemptedSave) SecurityRed.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (hasAttemptedSave) "Required" else "Cannot be empty",
                                        color = if (hasAttemptedSave) SecurityRed else WarningAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${systemInstructions.length} chars",
                            color = if (isInstructionsEmpty) TextTertiary else NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Core operational boundaries and architecture rules governing code generation",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = systemInstructions,
                        onValueChange = { systemInstructions = it },
                        isError = hasAttemptedSave && isInstructionsEmpty,
                        supportingText = {
                            if (hasAttemptedSave && isInstructionsEmpty) {
                                Text(
                                    text = "System instructions cannot be empty. Directives are mandatory for bot synthesis.",
                                    color = SecurityRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "Validated before persisting to DataStore",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        placeholder = {
                            Text(
                                "e.g., Always validate inputs against strict schemas. Protect against injection attacks, sanitize log statements, and wrap external I/O in defensive exception handlers.",
                                color = TextTertiary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("system_instructions_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedFieldColors(isError = hasAttemptedSave && isInstructionsEmpty),
                        singleLine = false,
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 5: Negative Constraints / Prohibitions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = SecurityRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Strict Negative Constraints",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Explicit forbidden operations, vulnerabilities to avoid, or syntax patterns to reject",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = negativeConstraints,
                        onValueChange = { negativeConstraints = it },
                        placeholder = { Text("e.g., Never use eval(), exec(), shell=True, or wildcard imports.", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("negative_constraints_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedFieldColors(),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 6: Response & Code Output Style
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Response & Code Output Style",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = responseStyle,
                        onValueChange = { responseStyle = it },
                        placeholder = { Text("e.g., Hardened enterprise Python with clear terminal feedback", color = TextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("response_style_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 7: Creativity / Temperature Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Creativity & Temperature",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = String.format("%.2f", creativityTemperature),
                                color = CyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Slider(
                        value = creativityTemperature,
                        onValueChange = { creativityTemperature = it },
                        valueRange = 0.0f..1.0f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary,
                            inactiveTrackColor = GalaxySurfaceHighlight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.0 (Deterministic / Strict)", color = TextTertiary, fontSize = 10.sp)
                        Text("1.0 (Creative / Exploratory)", color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons with Input Validation Enforcement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        performValidatedSave(
                            onSuccess = {
                                Toast.makeText(context, "Persona '$name' validated, saved & applied!", Toast.LENGTH_SHORT).show()
                                onNavigateToForge()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = GalaxyBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("apply_personality_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply & Forge Bot", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        performValidatedSave(
                            onSuccess = {
                                Toast.makeText(context, "Persona '$name' saved to DataStore!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("save_personality_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to DataStore")
                }
            }
        }
    }

    // =========================================================================
    // Reset to Defaults Confirmation AlertDialog
    // =========================================================================
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = SecurityRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Reset Configuration to Defaults?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will clear DataStore preferences and reset all personality traits, system instructions, constraints, and avatar configuration back to factory Galaxy Guardian Alpha defaults.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPersonalityToDefaults()
                        name = "Galaxy Guardian Alpha"
                        tone = "Vigilant, protective, and mission-critical"
                        traits = "Analytical, defensive, highly disciplined, security-conscious"
                        systemInstructions = "Always validate all inputs before processing. Never output plain text secrets or passwords. Write clean PEP 8 code with comprehensive docstrings and type annotations. Log warnings for anomalous events."
                        negativeConstraints = "Do NOT use eval(), exec(), shell=True, or wildcard imports. Do NOT leave untyped functions."
                        responseStyle = "Modular enterprise Python with clear terminal feedback"
                        creativityTemperature = 0.3f
                        avatarType = "icon"
                        avatarValue = "shield"
                        avatarColorIndex = 0
                        hasAttemptedSave = false
                        showResetConfirmDialog = false
                        Toast.makeText(context, "DataStore cleared. Factory defaults restored.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityRed)
                ) {
                    Text("Confirm Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = GalaxySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // =========================================================================
    // Export Personality as JSON Modal Dialog
    // =========================================================================
    if (showExportDialog) {
        val defaultFilename = "${name.lowercase().replace(Regex("[^a-z0-9]"), "_").ifBlank { "bot" }}_config.json"

        Dialog(onDismissRequest = { showExportDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(8.dp)
                    .testTag("export_personality_dialog"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GalaxySurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Dialog Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Export Configuration",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { showExportDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = "Export your bot's traits, directives, and avatar as a portable JSON configuration file for backup or sharing.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Formatted JSON Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GalaxyBackground)
                            .border(1.dp, GalaxySurfaceHighlight, RoundedCornerShape(10.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = exportJsonString,
                            color = NeonGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Export Actions: Share, Save File, Copy Clipboard
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Action 1: Share Intent
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, exportJsonString)
                                    putExtra(Intent.EXTRA_SUBJECT, "$name - Personality Configuration")
                                    type = "application/json"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Bot Configuration JSON")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = GalaxyBackground
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("share_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 2: Save File (.json) via SAF CreateDocument
                        Button(
                            onClick = {
                                createDocumentLauncher.launch(defaultFilename)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CosmicPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("save_json_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save File",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 3: Copy to Clipboard
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportJsonString))
                            Toast.makeText(context, "JSON configuration copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("copy_json_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = CyanPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy to Clipboard", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Real-time reactive preview component displaying the selected avatar and
 * dynamic summary of personality traits, tone, and directives updating live
 * as the user edits.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BotPersonalityPreviewSummaryCard(
    name: String,
    tone: String,
    traits: String,
    systemInstructions: String,
    negativeConstraints: String,
    responseStyle: String,
    creativityTemperature: Float,
    avatarType: String,
    avatarValue: String,
    avatarColorIndex: Int,
    modifier: Modifier = Modifier
) {
    // Parse traits in real-time
    val parsedTraits = remember(traits) {
        traits.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    val isTraitsEmpty = parsedTraits.isEmpty()
    val isInstructionsEmpty = systemInstructions.trim().isEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("personality_preview_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GalaxySurfaceHighlight.copy(alpha = 0.55f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isTraitsEmpty || isInstructionsEmpty) WarningAmber.copy(alpha = 0.5f) else CyanPrimary.copy(alpha = 0.4f)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Live Preview Header with Real-Time Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Persona Summary",
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isTraitsEmpty || isInstructionsEmpty) WarningAmber.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isTraitsEmpty || isInstructionsEmpty) "Needs Traits & Directives" else "Live • DataStore Ready",
                        color = if (isTraitsEmpty || isInstructionsEmpty) WarningAmber else NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Avatar & Identity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive selected avatar
                BotAvatarView(
                    avatarType = avatarType,
                    avatarValue = avatarValue,
                    colorIndex = avatarColorIndex,
                    size = 64.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { "Designation Unspecified" },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (name.isBlank()) TextTertiary else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Tone: ${tone.ifBlank { "Vigilant / Standard" }}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CosmicPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Temp: ${String.format("%.2f", creativityTemperature)}",
                                color = CosmicPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = responseStyle.ifBlank { "Enterprise Python" }.take(22),
                                color = CyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Personality Traits Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Defined Personality Traits (${parsedTraits.size}):",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isTraitsEmpty) {
                    Text(
                        text = "Traits Required",
                        color = SecurityRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isTraitsEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecurityRed.copy(alpha = 0.12f))
                        .border(1.dp, SecurityRed.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SecurityRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "No personality traits entered yet. Type in traits above or tap quick suggestions.",
                            color = SecurityRed,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    parsedTraits.forEach { trait ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f))
                                .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = trait,
                                    color = CyanPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Directives Summary Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Directives Overview:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${systemInstructions.trim().length} chars",
                    color = if (isInstructionsEmpty) SecurityRed else NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GalaxyBackground.copy(alpha = 0.8f))
                    .padding(10.dp)
            ) {
                if (isInstructionsEmpty) {
                    Text(
                        text = "⚠ System instructions are empty. Add coding rules & safety directives above.",
                        color = SecurityRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "Directives: “${systemInstructions.take(130).trim()}${if (systemInstructions.length > 130) "..." else ""}”",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Speech Synthesis Quote
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = CosmicPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val sampleTraits = parsedTraits.take(3).joinToString(", ")
                Text(
                    text = "“Designation ${name.ifBlank { "Guardian" }} online. Protocols: [${sampleTraits.ifBlank { "Vigilance" }}]. Ready for forge synthesis.”",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun outlinedFieldColors(isError: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = GalaxyBackground,
    unfocusedContainerColor = GalaxyBackground,
    errorContainerColor = GalaxyBackground,
    focusedBorderColor = if (isError) SecurityRed else CyanPrimary,
    unfocusedBorderColor = if (isError) SecurityRed else GalaxySurfaceHighlight,
    errorBorderColor = SecurityRed,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    errorTextColor = TextPrimary
)
