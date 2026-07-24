package com.attentionguard.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    isPlanActive: Boolean,
    onActivatePlan: () -> Unit,
    onModifyPlan: () -> Unit,
    onViewDashboard: () -> Unit
) {
    if (isPlanActive) {
        ActivePlanContent(
            onModifyPlan = onModifyPlan,
            onViewDashboard = onViewDashboard
        )
    } else {
        InactivePlanContent(
            apiScore = apiScore,
            riskTier = riskTier,
            onActivatePlan = onActivatePlan
        )
    }
}

@Composable
private fun ActivePlanContent(
    onModifyPlan: () -> Unit,
    onViewDashboard: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
            .verticalScroll(scrollState)
    ) {
        // Status Banner: Active Prevention Plan (Green)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF31A24C))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active Indicator",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Prevention Plan Active • Day 1 of 7",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "You're in Control",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark
                )
                Text(
                    text = "Your circuit-breaker plan is currently monitoring usage to help reset your focus.",
                    color = OnSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Active Measures Bento list
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Micro-Breaks Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SurfaceSoft, RoundedCornerShape(100.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = CommerceCobalt,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // Pulsing dot at top right
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFF31A24C), RoundedCornerShape(100.dp))
                                    .border(2.dp, Color.White, RoundedCornerShape(100.dp))
                                    .align(Alignment.TopEnd)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Micro-Breaks Enabled",
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark,
                                    fontSize = 15.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFFE6F4EA)
                                ) {
                                    Text(
                                        text = "Active",
                                        color = Color(0xFF1E7E34),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Next nudge in 12 minutes",
                                color = OnSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Nighttime Lockout Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceSoft, RoundedCornerShape(100.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = SecondaryGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Nighttime Lockout",
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark,
                                    fontSize = 15.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = SurfaceSoft
                                ) {
                                    Text(
                                        text = "Scheduled",
                                        color = OnSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Activates at 11:00 PM",
                                color = OnSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Focus Progress Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = CommerceCobalt,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Focus Progress",
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "20%",
                                color = CommerceCobalt,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = 0.2f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = CommerceCobalt,
                            trackColor = SurfaceSoft
                        )

                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = OnSurfaceDark))
                                append("0.4h of 2.0h")
                                pop()
                                append(" focus target reached today")
                            },
                            color = OnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }

                // Predicted Focus Recovery
                Card(
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Predicted Focus Recovery",
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark,
                            fontSize = 15.sp
                        )

                        // Custom Curved Path in Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(0f, size.height * 0.8f)
                                quadraticBezierTo(
                                    size.width * 0.3f, size.height * 0.75f,
                                    size.width * 0.5f, size.height * 0.55f
                                )
                                quadraticBezierTo(
                                    size.width * 0.75f, size.height * 0.25f,
                                    size.width, size.height * 0.1f
                                )
                            }
                            drawPath(
                                path = path,
                                color = CommerceCobalt,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawCircle(
                                color = CommerceCobalt,
                                radius = 4.dp.toPx(),
                                center = Offset(size.width, size.height * 0.1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current", fontSize = 11.sp, color = SecondaryGray)
                            Text("Next 4h", fontSize = 11.sp, color = SecondaryGray)
                            Text("Reset", fontSize = 11.sp, color = SecondaryGray)
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onModifyPlan,
                    border = BorderStroke(2.dp, CommerceCobalt),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CommerceCobalt),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Modify Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onViewDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = InkButton),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "View Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Footer
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Plan active since Oct 25, 2023.\nInsights update in real-time.",
                color = SecondaryGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InactivePlanContent(
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
private fun PlanCard(title: String, description: String, icon: ImageVector) {
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
