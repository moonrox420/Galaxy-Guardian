package com.example.galaxyguardian.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

val AvatarColorPalettes = listOf(
    Pair("Cyber Cyan", Color(0xFF00E5FF)),
    Pair("Cosmic Purple", Color(0xFFB388FF)),
    Pair("Neon Green", Color(0xFF00E676)),
    Pair("Warning Amber", Color(0xFFFFB300)),
    Pair("Shield Crimson", Color(0xFFFF5252)),
    Pair("Deep Azure", Color(0xFF2979FF))
)

val PredefinedIcons = listOf(
    Pair("shield", Icons.Default.Shield),
    Pair("robot", Icons.Default.SmartToy),
    Pair("brain", Icons.Default.Psychology),
    Pair("security", Icons.Default.Security),
    Pair("terminal", Icons.Default.Terminal),
    Pair("core", Icons.Default.Memory),
    Pair("bolt", Icons.Default.Bolt),
    Pair("radar", Icons.Default.Radar),
    Pair("satellite", Icons.Default.Satellite),
    Pair("lock", Icons.Default.Lock),
    Pair("rocket", Icons.Default.RocketLaunch),
    Pair("code", Icons.Default.Code),
    Pair("bug", Icons.Default.BugReport)
)

fun getIconForAvatar(key: String): ImageVector {
    return PredefinedIcons.firstOrNull { it.first == key }?.second ?: Icons.Default.Shield
}

@Composable
fun BotAvatarView(
    avatarType: String,
    avatarValue: String,
    colorIndex: Int,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val accentColor = AvatarColorPalettes.getOrElse(colorIndex) { AvatarColorPalettes.first() }.second

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        GalaxySurface,
                        GalaxyBackground
                    )
                )
            )
            .border(2.dp, accentColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarType == "procedural") {
            ProceduralCyberAvatarCanvas(
                seed = avatarValue,
                accentColor = accentColor,
                size = size
            )
        } else {
            val iconVector = getIconForAvatar(avatarValue)
            Icon(
                imageVector = iconVector,
                contentDescription = "Bot Avatar",
                tint = accentColor,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}

@Composable
fun ProceduralCyberAvatarCanvas(
    seed: String,
    accentColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val hash = seed.hashCode()
    val ringCount = 2 + (kotlin.math.abs(hash) % 3)
    val spokes = 4 + (kotlin.math.abs(hash shr 4) % 5)

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val maxRadius = (this.size.minDimension / 2f) - 6f

        // Draw concentric scanning rings
        for (i in 1..ringCount) {
            val radius = maxRadius * (i.toFloat() / ringCount.toFloat())
            drawCircle(
                color = accentColor.copy(alpha = 0.3f + (0.2f * i)),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )
        }

        // Draw radial spokes / reticle nodes
        val angleStep = (2 * Math.PI) / spokes
        for (i in 0 until spokes) {
            val angle = i * angleStep + (hash % 10) * 0.1
            val startRadius = maxRadius * 0.45f
            val endRadius = maxRadius * 0.95f
            val start = Offset(
                center.x + (cos(angle) * startRadius).toFloat(),
                center.y + (sin(angle) * startRadius).toFloat()
            )
            val end = Offset(
                center.x + (cos(angle) * endRadius).toFloat(),
                center.y + (sin(angle) * endRadius).toFloat()
            )
            drawLine(
                color = accentColor.copy(alpha = 0.6f),
                start = start,
                end = end,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Center quantum core node
        drawCircle(
            color = accentColor,
            radius = maxRadius * 0.22f,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = maxRadius * 0.08f,
            center = center
        )
    }
}

@Composable
fun BotAvatarPickerComponent(
    currentType: String,
    currentValue: String,
    currentColorIndex: Int,
    onAvatarChanged: (type: String, value: String, colorIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }

    fun triggerRandomize() {
        scope.launch {
            rotationAnim.animateTo(
                targetValue = rotationAnim.value + 360f,
                animationSpec = tween(400)
            )
        }
        val isProcedural = (0..1).random() == 1
        val randomColorIndex = (AvatarColorPalettes.indices).random()

        if (isProcedural) {
            val cyberSeeds = listOf("quantum_core", "neural_matrix", "sentinel_eye", "astro_shield", "pulsar_vortex", "cyber_hex", "titan_reticle")
            val randomSeed = "${cyberSeeds.random()}_${(100..999).random()}"
            onAvatarChanged("procedural", randomSeed, randomColorIndex)
        } else {
            val randomIcon = PredefinedIcons.random().first
            onAvatarChanged("icon", randomIcon, randomColorIndex)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bot Identity & Avatar",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { triggerRandomize() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary.copy(alpha = 0.15f),
                        contentColor = CyanPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("randomize_avatar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Randomize",
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(rotationAnim.value)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Randomize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Preview Display + Style Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotAvatarView(
                    avatarType = currentType,
                    avatarValue = currentValue,
                    colorIndex = currentColorIndex,
                    size = 76.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (currentType == "procedural") "AI Quantum Core" else "Vector Glyph (${currentValue.replaceFirstChar { it.uppercase() }})",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Custom visual signature for your synthesized bot",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = currentType == "icon",
                            onClick = { onAvatarChanged("icon", currentValue, currentColorIndex) },
                            label = { Text("Predefined Glyphs", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = GalaxySurfaceHighlight,
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = CyanPrimary
                            )
                        )
                        FilterChip(
                            selected = currentType == "procedural",
                            onClick = { onAvatarChanged("procedural", currentValue, currentColorIndex) },
                            label = { Text("AI Reticle Core", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = GalaxySurfaceHighlight,
                                selectedContainerColor = CosmicPurple.copy(alpha = 0.25f),
                                selectedLabelColor = CosmicPurple
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Palette Color Selector
            Text(
                text = "Aura Glow Palette:",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvatarColorPalettes.forEachIndexed { index, (name, color) ->
                    val isSelected = currentColorIndex == index
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else GalaxySurfaceHighlight,
                                shape = CircleShape
                            )
                            .clickable { onAvatarChanged(currentType, currentValue, index) }
                    )
                }
            }

            if (currentType == "icon") {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Select Predefined Icon:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Predefined icons grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val previewIcons = PredefinedIcons.take(6)
                    previewIcons.forEach { (key, iconVec) ->
                        val isSelected = currentValue == key
                        val activeColor = AvatarColorPalettes.getOrElse(currentColorIndex) { AvatarColorPalettes.first() }.second
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else GalaxySurfaceHighlight)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) activeColor else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onAvatarChanged("icon", key, currentColorIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = key,
                                tint = if (isSelected) activeColor else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val previewIcons = PredefinedIcons.drop(6).take(6)
                    previewIcons.forEach { (key, iconVec) ->
                        val isSelected = currentValue == key
                        val activeColor = AvatarColorPalettes.getOrElse(currentColorIndex) { AvatarColorPalettes.first() }.second
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else GalaxySurfaceHighlight)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) activeColor else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onAvatarChanged("icon", key, currentColorIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = key,
                                tint = if (isSelected) activeColor else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
