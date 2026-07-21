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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideogameAsset
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
    onDismiss: () -> Unit
) {
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
                    text = "You've spent 40% more time on short-form videos after midnight this week. Taking a short break can help restore your attention span tomorrow.",
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
                    // Alert Hero Card (32.dp rounding)
                    Card(
                        shape = RoundedCornerShape(32.dp),
                        border = BorderStroke(1.dp, HairlineSoft),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header Image placeholder box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(SurfaceSoft)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = RiskHigh,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = String.format("High Risk Detected (API >= 0.65)", apiScore),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Attention Pattern Alert",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark
                                )
                                Text(
                                    text = "Your attention performance score indicates severe pattern fragmentation over the last 24 hours. This suggests high cognitive load or sustained external distractions.",
                                    fontSize = 14.sp,
                                    color = OnSurfaceVariant,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = "This is an AI self-awareness nudge, not a medical diagnosis.",
                                    fontSize = 11.sp,
                                    color = SecondaryGray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }

                    // Suggested steps (Checkboxes change container border & background)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Suggested Steps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )

                        StepCheckboxRow(
                            title = "Enable Micro-Breaks",
                            subtitle = "System-wide 30-second haptic nudges every 25 minutes.",
                            checked = check1,
                            onCheckedChange = { check1 = it },
                            icon = Icons.Default.Timer
                        )

                        StepCheckboxRow(
                            title = "Late-Night Window Lock",
                            subtitle = "Restrict non-essential tabs from 11 PM to 6 AM.",
                            checked = check2,
                            onCheckedChange = { check2 = it },
                            icon = Icons.Default.Bedtime
                        )

                        StepCheckboxRow(
                            title = "Focus Mini-Games",
                            subtitle = "Short, science-backed games to reset neural pathways during high load.",
                            checked = check3,
                            onCheckedChange = { check3 = it },
                            icon = Icons.Default.VideogameAsset
                        )
                    }

                    // Cobalt Blue CTA Button (strictly reserved for critical action)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
                    ) {
                        Button(
                            onClick = onActivate,
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
fun StepCheckboxRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    // Styling states change when checked
    val borderStroke = if (checked) BorderStroke(1.dp, CommerceCobalt) else BorderStroke(1.dp, HairlineSoft)
    val background = if (checked) SurfaceSoft else Color.White

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
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = CommerceCobalt,
                    uncheckedColor = Hairline,
                    checkmarkColor = Color.White
                )
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
}
