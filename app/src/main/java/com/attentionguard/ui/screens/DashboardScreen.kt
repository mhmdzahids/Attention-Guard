package com.attentionguard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attentionguard.ui.theme.*

@Composable
fun DashboardScreen(
    apiScore: Float,
    riskTier: String,
    sessionDuration: Float,
    scrollVelocity: Float,
    switchFreq: Float,
    nightRatio: Float
) {
    val scrollState = rememberScrollState()

    var gaugeProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(apiScore) {
        gaugeProgress = apiScore
    }

    val animatedScore by animateFloatAsState(
        targetValue = gaugeProgress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today Focus Level Header Badge
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                color = SurfaceSoft,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "TODAY'S ATTENTION INDEX",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    color = CommerceCobalt,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Hero Circular Gauge (32.dp corner rounding xxxl)
        Card(
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, HairlineSoft),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(192.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val strokeColor = when (riskTier) {
                        "low" -> RiskLow
                        "moderate" -> RiskModerate
                        else -> RiskHigh
                    }
                    // Canvas to draw Custom Circular Gauge Arc
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 26f
                        // Track Background
                        drawCircle(
                            color = SurfaceSoft,
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress Arc
                        drawArc(
                            color = strokeColor,
                            startAngle = -90f,
                            sweepAngle = animatedScore * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.2f", animatedScore),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                        Text(
                            text = "API SCORE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGray,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Risk Badge
                val badgeColor = when (riskTier) {
                    "low" -> RiskLow
                    "moderate" -> RiskModerate
                    else -> RiskHigh
                }
                val riskLabel = when (riskTier) {
                    "low" -> "Low Risk"
                    "moderate" -> "Moderate Risk"
                    else -> "High Risk"
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = riskLabel,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Attention Performance Indicator reflects your digital cognitive load today.",
                    textAlign = TextAlign.Center,
                    color = OnSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.widthIn(max = 240.dp)
                )
            }
        }

        // Metrics Grid (16.dp corner rounding xl, 1.dp hairline border)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Session Duration",
                value = String.format("%.1f hrs", sessionDuration),
                subtitle = "UsageStats API",
                icon = Icons.Default.Timer
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Scroll Speed",
                value = if (scrollVelocity > 160f) "Fast" else if (scrollVelocity < 80f) "Slow" else "Normal",
                subtitle = "Accessibility API",
                icon = Icons.Default.Speed
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Task Switches",
                value = String.format("%d/hr", switchFreq.toInt()),
                subtitle = "ActivityManager API",
                icon = Icons.Default.AltRoute
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Night-time Use",
                value = String.format("%d%%", (nightRatio * 100).toInt()),
                subtitle = "Midnight Ratio",
                icon = Icons.Default.DarkMode
            )
        }

        // Detailed Recommendation Bento Box (32.dp corner rounding xxxl)
        Card(
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, HairlineSoft),
            colors = CardDefaults.cardColors(containerColor = SurfaceSoft),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    color = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Recommendation icon",
                            tint = CommerceCobalt,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Recommendation",
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark,
                        fontSize = 15.sp
                    )
                    val recText = when (riskTier) {
                        "low" -> "Your digital attention patterns are healthy. Maintain current habits."
                        "moderate" -> "Mild scrolling acceleration detected. A quick 5-min breathing session is advised."
                        else -> "High task switching and post-midnight usage. Enabling Lockout mode recommended."
                    }
                    Text(
                        text = recText,
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HairlineSoft),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = CommerceCobalt,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    color = SecondaryGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column {
                Text(
                    text = value,
                    color = OnSurfaceDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = SecondaryGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
