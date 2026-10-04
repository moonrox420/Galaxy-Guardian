package com.example.galaxyguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.data.repository.ThemeMode
import com.example.galaxyguardian.ui.screens.BotLibraryScreen
import com.example.galaxyguardian.ui.screens.HomeScreen
import com.example.galaxyguardian.ui.screens.PersonalityScreen
import com.example.galaxyguardian.ui.screens.SettingsScreen
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxyBackground
import com.example.galaxyguardian.ui.theme.GalaxyGuardianTheme
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.GalaxyTheme
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextTertiary
import com.example.galaxyguardian.ui.viewmodel.GalaxyGuardianViewModel

enum class Screen {
    HOME,
    PERSONALITY,
    LIBRARY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: GalaxyGuardianViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            GalaxyGuardianTheme(themeMode = themeMode) {
                MainAppShell(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppShell(viewModel: GalaxyGuardianViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(GalaxyBackground)
        ) {
            NavigationRail(
                containerColor = GalaxySurface,
                contentColor = CyanPrimary
            ) {
                NavigationRailItem(
                    selected = currentScreen == Screen.HOME,
                    onClick = { currentScreen = Screen.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.HOME) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Generator",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Forge", fontSize = 12.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = GalaxyBackground,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )

                NavigationRailItem(
                    selected = currentScreen == Screen.PERSONALITY,
                    onClick = { currentScreen = Screen.PERSONALITY },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.PERSONALITY) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                            contentDescription = "Personality",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Persona", fontSize = 12.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = GalaxyBackground,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )

                NavigationRailItem(
                    selected = currentScreen == Screen.LIBRARY,
                    onClick = { currentScreen = Screen.LIBRARY },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.LIBRARY) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Library",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Library", fontSize = 12.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = GalaxyBackground,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )

                NavigationRailItem(
                    selected = currentScreen == Screen.SETTINGS,
                    onClick = { currentScreen = Screen.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == Screen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Settings", fontSize = 12.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = GalaxyBackground,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Wide-Screen Theme Toggle Button
                val isDark = GalaxyTheme.colors.isDark
                IconButton(
                    onClick = {
                        viewModel.setThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK)
                    },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = CyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPersonality = { currentScreen = Screen.PERSONALITY },
                        onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                    )
                    Screen.PERSONALITY -> PersonalityScreen(viewModel = viewModel, onNavigateToForge = { currentScreen = Screen.HOME })
                    Screen.LIBRARY -> BotLibraryScreen(viewModel = viewModel, onNavigateToHome = { currentScreen = Screen.HOME })
                    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    } else {
        Scaffold(
            containerColor = GalaxyBackground,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GalaxySurface)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentScreen) {
                                Screen.HOME -> "Galaxy Guardian"
                                Screen.PERSONALITY -> "Bot Directives & Persona"
                                Screen.LIBRARY -> "Saved Sentinel Bots"
                                Screen.SETTINGS -> "System Settings"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val isDark = GalaxyTheme.colors.isDark
                    IconButton(
                        onClick = {
                            viewModel.setThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK)
                        },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = GalaxySurface,
                    contentColor = CyanPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { currentScreen = Screen.HOME },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.HOME) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "Generator"
                            )
                        },
                        label = { Text("Forge", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GalaxyBackground,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.PERSONALITY,
                        onClick = { currentScreen = Screen.PERSONALITY },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.PERSONALITY) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                                contentDescription = "Personality"
                            )
                        },
                        label = { Text("Persona", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GalaxyBackground,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.LIBRARY,
                        onClick = { currentScreen = Screen.LIBRARY },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.LIBRARY) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Library"
                            )
                        },
                        label = { Text("Library", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GalaxyBackground,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.SETTINGS,
                        onClick = { currentScreen = Screen.SETTINGS },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == Screen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GalaxyBackground,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPersonality = { currentScreen = Screen.PERSONALITY },
                        onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                    )
                    Screen.PERSONALITY -> PersonalityScreen(viewModel = viewModel, onNavigateToForge = { currentScreen = Screen.HOME })
                    Screen.LIBRARY -> BotLibraryScreen(viewModel = viewModel, onNavigateToHome = { currentScreen = Screen.HOME })
                    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
