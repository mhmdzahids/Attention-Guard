package com.attentionguard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attentionguard.ui.theme.*

@Composable
fun MeditateScreen(
    apiScore: Float,
    riskTier: String,
    onActivatePlan: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
            .verticalScroll(scrollState)
    ) {
        // High Priority Status Banner (adheres strictly to Meta Design System)
        val bannerColor = when (riskTier) {
            "low" -> RiskLow
            "moderate" -> RiskModerate
            else -> RiskHigh
        }
        val riskLabel = when (riskTier) {
            "low" -> "Low Attention Risk"
            "moderate" -> "Moderate Attention Risk"
            else -> "High Attention Risk"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerColor)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = String.format("%s (API: %.2f)", riskLabel, apiScore),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Title
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Let's Reset Your Focus",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark
                )
                Text(
                    text = "Based on your recent app usage patterns, we've designed a specialized circuit-breaker plan to help you regain control.",
                    color = OnSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Recommendations Bento List
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PlanCard(
                    title = "Enable Micro-Breaks",
                    description = "Automatically trigger gentle reminders every 20 minutes of scrolling.",
                    icon = Icons.Default.Timer
                )
                PlanCard(
                    title = "Nighttime Lockout",
                    description = "Recommendations to restrict app launches post-midnight to protect sleep hygiene.",
                    icon = Icons.Default.Bedtime
                )
                PlanCard(
                    title = "Focus Mini-Games",
                    description = "Sharpen your mind with quick cognitive games once your scrolling timer expires to help regain concentration.",
                    icon = Icons.Default.Extension
                )
            }

            // Predicted growth banner
            Card(
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = SurfaceSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Implementing this plan is predicted to increase your focus window by 42% over the next 7 days.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(CommerceCobalt, RoundedCornerShape(100.dp))
                            )
                            Text("Planned Growth", fontSize = 11.sp, color = SecondaryGray)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Hairline, RoundedCornerShape(100.dp))
                            )
                            Text("Current Trend", fontSize = 11.sp, color = SecondaryGray)
                        }
                    }
                }
            }

            // Cobalt Blue CTA Button (strictly reserved for critical action)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onActivatePlan,
                    colors = ButtonDefaults.buttonColors(containerColor = CommerceCobalt),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Activate Prevention Plan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "This is an AI-assisted self-awareness tool, not a clinical diagnosis.",
                    fontSize = 11.sp,
                    color = SecondaryGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PlanCard(title: String, description: String, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HairlineSoft),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = SurfaceSoft,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = CommerceCobalt,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark,
                    fontSize = 15.sp
                )
                Text(
                    text = description,
                    color = OnSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
