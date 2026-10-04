package com.example.galaxyguardian.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.data.model.BotProject
import com.example.galaxyguardian.data.model.BotTemplate
import com.example.galaxyguardian.data.model.TargetLanguage
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
import com.example.galaxyguardian.ui.viewmodel.GalaxyGuardianViewModel

@Composable
fun BotLibraryScreen(
    viewModel: GalaxyGuardianViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedBots by viewModel.savedBots.collectAsState()
    val templates = viewModel.templates
    var selectedTab by remember { mutableStateOf(0) } // 0 = Saved Projects, 1 = Presets
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Kotlin/Android", "Python", "Weather", "Security", "DevOps", "Database")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredBots = savedBots.filter { bot ->
        val matchesSearch = bot.title.contains(searchQuery, ignoreCase = true) ||
                bot.prompt.contains(searchQuery, ignoreCase = true) ||
                bot.generatedCode.contains(searchQuery, ignoreCase = true) ||
                bot.formattedCode.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "Kotlin/Android" -> bot.toTargetLanguage() == TargetLanguage.KOTLIN_ANDROID
            "Python" -> bot.toTargetLanguage() == TargetLanguage.PYTHON
            "Weather" -> bot.prompt.contains("weather", true) || bot.prompt.contains("forecast", true)
            "Security" -> bot.prompt.contains("security", true) || bot.prompt.contains("discord", true) || bot.prompt.contains("moder", true)
            "DevOps" -> bot.prompt.contains("github", true) || bot.prompt.contains("k8s", true) || bot.prompt.contains("kubernetes", true) || bot.prompt.contains("devops", true)
            "Database" -> bot.prompt.contains("backup", true) || bot.prompt.contains("database", true) || bot.prompt.contains("room", true) || bot.prompt.contains("db", true)
            else -> true
        }
        matchesSearch && matchesCategory
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
                .padding(bottom = 80.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Project Library & Blueprints",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Browse saved Kotlin / Python projects and preset recipes",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Tabs: Saved Projects vs Presets
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = GalaxySurface,
                contentColor = CyanPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Saved Projects (${savedBots.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Presets (${templates.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects, prompts, or source code...", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GalaxySurface,
                        unfocusedContainerColor = GalaxySurface,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = GalaxySurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary,
                                containerColor = GalaxySurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredBots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching projects found" else "No saved projects yet",
                                color = TextSecondary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Generate Android features or Python scripts on the Home screen and tap Save to store them here.",
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredBots, key = { it.id }) { bot ->
                            SavedBotCard(
                                bot = bot,
                                onLoad = {
                                    viewModel.loadBot(bot)
                                    onNavigateToHome()
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(bot.id) },
                                onDelete = { viewModel.deleteBot(bot.id) }
                            )
                        }
                    }
                }
            } else {
                // Templates list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(templates, key = { it.id }) { tmpl ->
                        TemplateCard(
                            template = tmpl,
                            onSelect = {
                                viewModel.loadTemplate(tmpl)
                                onNavigateToHome()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedBotCard(
    bot: BotProject,
    onLoad: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val langEnum = bot.toTargetLanguage()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bot.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (bot.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (bot.isFavorite) SecurityRed else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = bot.prompt,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val langColor = if (langEnum == TargetLanguage.KOTLIN_ANDROID) CyanPrimary else NeonGreen
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(langColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = langEnum.displayName,
                            color = langColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Score: ${bot.analysis.securityScore}/100",
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (bot.providerType.isNotBlank()) {
                        val badgeColor = when (bot.providerType) {
                            "OLLAMA" -> NeonGreen
                            "OFFLINE_ONLY", "OFFLINE_BASELINE" -> CosmicPurple
                            else -> CyanPrimary
                        }
                        val badgeLabel = when (bot.providerType) {
                            "OLLAMA" -> "Ollama"
                            "OFFLINE_ONLY", "OFFLINE_BASELINE" -> "Offline"
                            else -> "Gemini"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Load Project",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: BotTemplate,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyanPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ready to Run",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = template.description,
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GalaxyBackground)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Prompt: \"${template.prompt}\"",
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
