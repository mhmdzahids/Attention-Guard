package com.attentionguard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attentionguard.ui.theme.*

@Composable
fun InsightsScreen(
    apiScore: Float,
    riskTier: String,
    sessionDuration: Float,
    scrollVelocity: Float,
    switchFreq: Float,
    nightRatio: Float,
    skipFrequency: Float,
    onNavigateToMeditate: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 100.dp), // offset for sticky bottom button
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "24-Hour Behavioral Pattern",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceDark
            )

            // Chart Bento Card (32.dp rounding)
            Card(
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "PEAK ACTIVITY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "01:42 AM",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = SurfaceSoft,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "+18% vs yesterday",
                                color = CommerceCobalt,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SVG-style line chart drawing inside Compose Canvas
                    val graphScale = apiScore / 0.52f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw baseline grid lines
                            drawLine(
                                color = HairlineSoft,
                                start = Offset(0f, h),
                                end = Offset(w, h),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = SurfaceSoft,
                                start = Offset(0f, h / 2f),
                                end = Offset(w, h / 2f),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )

                            // Define bezier control points scaled dynamically
                            val p1 = Offset(0f, h)
                            val p2 = Offset(w * 0.15f, h * 0.85f * graphScale)
                            val p3 = Offset(w * 0.30f, h * 0.40f * graphScale)
                            val p4 = Offset(w * 0.45f, h * 0.15f * graphScale)
                            val p5 = Offset(w * 0.60f, h * 0.70f * graphScale)
                            val p6 = Offset(w * 0.75f, h * 0.80f * graphScale)
                            val p7 = Offset(w * 0.90f, h * 0.35f * graphScale)
                            val p8 = Offset(w, h * 0.40f * graphScale)

                            val path = Path().apply {
                                moveTo(p1.x, p1.y)
                                cubicTo(p2.x, p2.y, p3.x, p3.y, p4.x, p4.y)
                                cubicTo(p5.x, p5.y, p6.x, p6.y, p7.x, p7.y)
                                lineTo(p8.x, p8.y)
                            }

                            // Draw Area Gradient Fill
                            val areaPath = Path().apply {
                                addPath(path)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            val themeColor = when (riskTier) {
                                "low" -> RiskLow
                                "moderate" -> RiskModerate
                                else -> RiskHigh
                            }

                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(themeColor.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )

                            // Draw Smooth Path Line
                            drawPath(
                                path = path,
                                color = themeColor,
                                style = Stroke(width = 8f, cap = StrokeCap.Round)
                            )

                            // Draw Anomaly Highlight circle (Peak point around 45%)
                            val highlightX = w * 0.45f
                            val highlightY = h * 0.15f * graphScale
                            drawCircle(
                                color = themeColor,
                                radius = 15f,
                                center = Offset(highlightX, highlightY),
                                style = Stroke(width = 4f)
                            )
                            drawCircle(
                                color = themeColor,
                                radius = 6f,
                                center = Offset(highlightX, highlightY)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("12 PM", "6 PM", "12 AM", "6 AM").forEach { time ->
                            Text(
                                text = time,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGray
                            )
                        }
                    }
                }
            }

            // Signal Breakdown Stack (16.dp corner rounding xl cards)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. Session Dynamics
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = "Session",
                                        tint = CommerceCobalt,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Session Dynamics",
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Average usage duration today",
                                    color = SecondaryGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        val sessionRatio = sessionDuration / 8f
                        val animWidth by animateFloatAsState(
                            targetValue = sessionRatio,
                            animationSpec = tween(1200)
                        )
                        
                        // Custom Linear Progress bar with dynamic color
                        val barColor = when {
                            animWidth > 0.65f -> RiskHigh
                            animWidth > 0.35f -> RiskModerate
                            else -> RiskLow
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(SurfaceSoft)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animWidth)
                                    .background(barColor)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format("%d%% Intensity", (sessionRatio * 100).toInt()),
                                color = SecondaryGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format("%.1fh Total", sessionDuration),
                                color = OnSurfaceDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Interaction Micro-Behaviors
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Interaction Micro-Behaviors",
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark,
                            fontSize = 15.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceSoft),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Scroll Speed",
                                        color = SecondaryGray,
                                        fontSize = 12.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.0f", scrollVelocity),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceDark
                                        )
                                        Text(
                                            text = " px/s",
                                            fontSize = 12.sp,
                                            color = SecondaryGray,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }
                            }

                            val skipColor = if (skipFrequency > 55f) RiskHigh else OnSurfaceDark
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceSoft),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Skip Rate",
                                        color = SecondaryGray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = String.format("%.0f%%", skipFrequency),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = skipColor,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Task Switching
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                                        imageVector = Icons.Default.SyncAlt,
                                        contentDescription = "Switches",
                                        tint = CommerceCobalt,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                textLabel(text = "Task Switching")
                                Text(
                                    text = "Rapid Inter-app Switches",
                                    color = SecondaryGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = String.format("%.1f", switchFreq),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }
                }

                // 4. Temporal Distribution
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(4.dp, CommerceCobalt), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            textLabel(text = "Temporal Distribution")
                            Text(
                                text = "Post-midnight Session Ratio",
                                color = SecondaryGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SyncAlt,
                                    contentDescription = "Midnight",
                                    tint = CommerceCobalt,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Midnight usage detected",
                                    color = CommerceCobalt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Circular Progress indicator
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = SurfaceSoft,
                                    style = Stroke(width = 10f)
                                )
                                drawArc(
                                    color = CommerceCobalt,
                                    startAngle = -90f,
                                    sweepAngle = nightRatio * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = String.format("%d%%", (nightRatio * 100).toInt()),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark
                            )
                        }
                    }
                }
            }
        }

        // Sticky Bottom Button Area (using Meta palette primary CTA: 100px Black Pill Button shape)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onNavigateToMeditate,
                    colors = ButtonDefaults.buttonColors(containerColor = InkButton),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "View Prevention Plan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun textLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = OnSurfaceDark,
        fontSize = 15.sp
    )
}
