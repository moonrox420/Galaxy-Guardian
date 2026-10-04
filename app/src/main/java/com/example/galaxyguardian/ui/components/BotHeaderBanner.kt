package com.example.galaxyguardian.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galaxyguardian.ui.theme.CosmicPurple
import com.example.galaxyguardian.ui.theme.CyanPrimary
import com.example.galaxyguardian.ui.theme.GalaxyBackground
import com.example.galaxyguardian.ui.theme.GalaxySurface
import com.example.galaxyguardian.ui.theme.NeonGreen
import com.example.galaxyguardian.ui.theme.TextPrimary
import com.example.galaxyguardian.ui.theme.TextSecondary
import kotlin.random.Random

@Composable
fun BotHeaderBanner(
    modifier: Modifier = Modifier,
    securityScore: Int? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GalaxySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            // Bulletproof Procedural Cosmic Starfield & Nebula Background
            val bgCol = GalaxyBackground
            val purpleCol = CosmicPurple
            val cyanCol = CyanPrimary

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background deep galaxy gradient
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            bgCol,
                            purpleCol.copy(alpha = 0.25f),
                            cyanCol.copy(alpha = 0.20f),
                            bgCol
                        )
                    )
                )

                // Nebula glowing orbs
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(purpleCol.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.75f, size.height * 0.35f),
                        radius = size.height * 0.9f
                    ),
                    center = Offset(size.width * 0.75f, size.height * 0.35f),
                    radius = size.height * 0.9f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(cyanCol.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(size.width * 0.25f, size.height * 0.8f),
                        radius = size.height * 0.7f
                    ),
                    center = Offset(size.width * 0.25f, size.height * 0.8f),
                    radius = size.height * 0.7f
                )

                // Deterministic cosmic starfield
                val starRandom = Random(42)
                for (i in 0 until 40) {
                    val x = starRandom.nextFloat() * size.width
                    val y = starRandom.nextFloat() * size.height
                    val starRadius = if (i % 5 == 0) 2.0f else 1.2f
                    val starAlpha = 0.3f + (starRandom.nextFloat() * 0.6f)
                    drawCircle(
                        color = Color.White.copy(alpha = starAlpha),
                        radius = starRadius,
                        center = Offset(x, y)
                    )
                }
            }

            // Dark vignette overlay for contrast and typography readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GalaxyBackground.copy(alpha = 0.88f),
                                GalaxyBackground.copy(alpha = 0.60f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Content on top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vector-based Guardian Shield Avatar badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyanPrimary.copy(alpha = 0.4f),
                                    CosmicPurple.copy(alpha = 0.2f),
                                    GalaxySurface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Galaxy Guardian Shield",
                        tint = CyanPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Galaxy Guardian",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security Shield",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "AI Bot Forge • Static Analysis • Lifecycle Sim",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Python • PEP 8 • Security Scan",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (securityScore != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Score: $securityScore/100",
                                color = CyanPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
}
