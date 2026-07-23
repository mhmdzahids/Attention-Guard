package com.attentionguard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attentionguard.ui.theme.*

@Composable
fun NudgeModal(
    apiScore: Float,
    dbLogs: List<com.attentionguard.data.AttentionLog>,
    onDismiss: () -> Unit
) {
    val lateNightText = remember(dbLogs) {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
        val weeklyLogs = dbLogs.filter { it.timestamp >= sevenDaysAgo }
        
        if (weeklyLogs.isNotEmpty()) {
            val avgNightRatio = weeklyLogs.map { it.nightRatio }.average().toFloat()
            val baseline = 0.10f
            if (avgNightRatio > baseline) {
                val percentIncrease = ((avgNightRatio - baseline) / baseline * 100f).toInt()
                "You've spent $percentIncrease% more time on short-form videos after midnight this week. Taking a short break can help restore your attention span tomorrow."
            } else {
                val usagePercent = (avgNightRatio * 100f).toInt()
                "You've spent $usagePercent% of your short-form video time after midnight this week. Taking a short break can help restore your attention span tomorrow."
            }
        } else {
            "You've spent 40% more time on short-form videos after midnight this week. Taking a short break can help restore your attention span tomorrow."
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp), // rounded-xxl (24.dp)
            border = BorderStroke(1.dp, HairlineSoft),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Warning Yellow Badge
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = RiskModerate
                ) {
                    Text(
                        text = "ATTENTION NUDGE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                // Self improvement icon
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Nudge Icon",
                    tint = CommerceCobalt,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "Late-Night Scrolling Detected",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = lateNightText,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Text(
                    text = String.format("API Score: %.2f", apiScore),
                    fontSize = 12.sp,
                    color = SecondaryGray,
                    fontWeight = FontWeight.Bold
                )

                // Black 100px primary pill button CTA
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = InkButton),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Got It",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ghost / Secondary Outline Button
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(2.dp, OnSurfaceDark),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Set Reminder",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PreventionPlanOverlayModal(
    apiScore: Float,
    onDismiss: () -> Unit,
    onActivate: () -> Unit
) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }
    var check4 by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Top Close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 48.dp, bottom = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SurfaceSoft,
                        border = BorderStroke(1.dp, HairlineSoft),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close overlay",
                                tint = OnSurfaceDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Customize your plan title
                    Text(
                        text = "Customize your plan",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark
                    )

                    // Suggested steps (Checkboxes change container border & background)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StepCheckboxRow(
                            title = "Enable Micro-Breaks",
                            subtitle = "System-wide 30-second haptic nudges every 25 minutes.",
                            checked = check1,
                            onCheckedChange = { 
                                check1 = it
                                if (it) check4 = false
                            },
                            icon = Icons.Default.Timer
                        )

                        StepCheckboxRow(
                            title = "Late-Night Window Lock",
                            subtitle = "Restrict non-essential tabs from 11 PM to 6 AM.",
                            checked = check2,
                            onCheckedChange = { 
                                check2 = it
                                if (it) check4 = false
                            },
                            icon = Icons.Default.Bedtime
                        )

                        StepCheckboxRow(
                            title = "Focus Mini-Games",
                            subtitle = "Short, science-backed games to reset neural pathways during high cognitive load.",
                            checked = check3,
                            onCheckedChange = { 
                                check3 = it
                                if (it) check4 = false
                            },
                            icon = Icons.Default.VideogameAsset
                        )

                        StepCheckboxRow(
                            title = "Let The System Decide",
                            subtitle = "AI-optimized protection based on your unique behavioral patterns and fatigue signals.",
                            checked = check4,
                            onCheckedChange = { 
                                check4 = it
                                if (it) {
                                    check1 = false
                                    check2 = false
                                    check3 = false
                                }
                            },
                            icon = Icons.Default.SelfImprovement,
                            isRecommended = true
                        )
                    }

                    // Bottom Action Cluster
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
                    ) {
                        Button(
                            onClick = onActivate,
                            colors = ButtonDefaults.buttonColors(containerColor = InkButton),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Confirm Activation",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Remind Me Later - Black Outlined button
                        OutlinedButton(
                            onClick = onDismiss,
                            border = BorderStroke(2.dp, OnSurfaceDark),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Remind Me Later",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(if (checked) CommerceCobalt else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (checked) CommerceCobalt else Hairline,
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun StepCheckboxRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    isRecommended: Boolean = false
) {
    // Styling states change when checked
    val borderStroke = if (checked) BorderStroke(1.dp, CommerceCobalt) else BorderStroke(1.dp, HairlineSoft)
    val background = if (checked) SurfaceSoft else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isRecommended) 8.dp else 0.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            border = borderStroke,
            colors = CardDefaults.cardColors(containerColor = background),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularCheckbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitle,
                        color = SecondaryGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (checked) CommerceCobalt else SecondaryGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (isRecommended) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = PromoGold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = (-8).dp)
            ) {
                Text(
                    text = "Recommended",
                    color = OnSurfaceDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    maxLines = 1
                )
            }
        }
    }
}
