package com.attentionguard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attentionguard.ui.theme.*

@Composable
fun SettingsScreen(
    useSimulatedData: Boolean,
    onSimulatedDataToggled: (Boolean) -> Unit,
    sessionDuration: Float,
    onSessionChanged: (Float) -> Unit,
    launchFrequency: Int,
    onLaunchesChanged: (Int) -> Unit,
    scrollVelocity: Float,
    onScrollChanged: (Float) -> Unit,
    skipFrequency: Float,
    onSkipsChanged: (Float) -> Unit,
    switchFreq: Float,
    onSwitchesChanged: (Float) -> Unit,
    foregroundLatency: Float,
    onLatencyChanged: (Float) -> Unit,
    nightRatio: Float,
    onNightChanged: (Float) -> Unit,
    onSeedTestData: () -> Unit,
    onTriggerNudgeModal: () -> Unit
) {
    val scrollState = rememberScrollState()

    var localStorageOnly by remember { mutableStateOf(true) }
    var quietHours by remember { mutableStateOf(true) }
    var monitorSession by remember { mutableStateOf(true) }
    var monitorMicro by remember { mutableStateOf(true) }
    var monitorSwitch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section A: Measurement Mode Toggle
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderSection(title = "Measurement Mode", icon = Icons.Default.Science)
            
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                ToggleRow(
                    title = "Real-world Sensor Mode",
                    subtitle = "Gunakan sensor nyata Android OS (UsageStats & Accessibility) untuk mengkalkulasi API Score secara otomatis.",
                    checked = !useSimulatedData,
                    onCheckedChange = { onSimulatedDataToggled(!it) }
                )
            }
        }

        // Section B: Signal Simulator
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderSection(title = "Signal Simulator", icon = Icons.Default.Science)
            
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Slider 1: Session Duration
                    SliderItem(
                        label = "Session Duration",
                        value = sessionDuration,
                        range = 0f..8.0f,
                        valueLabel = String.format("%.1f hrs", sessionDuration),
                        enabled = useSimulatedData,
                        onValueChange = onSessionChanged
                    )

                    // Slider 2: Launch count
                    SliderItem(
                        label = "Launch Frequency",
                        value = launchFrequency.toFloat(),
                        range = 0f..30f,
                        valueLabel = "$launchFrequency launches",
                        enabled = useSimulatedData,
                        onValueChange = { onLaunchesChanged(it.toInt()) }
                    )

                    // Slider 3: Scroll speed
                    SliderItem(
                        label = "Scroll Velocity",
                        value = scrollVelocity,
                        range = 0f..250f,
                        valueLabel = String.format("%.0f px/s", scrollVelocity),
                        enabled = useSimulatedData,
                        onValueChange = onScrollChanged
                    )

                    // Slider 4: Skip speed
                    SliderItem(
                        label = "Skip Frequency",
                        value = skipFrequency,
                        range = 0f..100f,
                        valueLabel = String.format("%.0f%%", skipFrequency),
                        enabled = useSimulatedData,
                        onValueChange = onSkipsChanged
                    )

                    // Slider 5: Switches
                    SliderItem(
                        label = "Task Switches",
                        value = switchFreq,
                        range = 0f..20f,
                        valueLabel = String.format("%.1f/hr", switchFreq),
                        enabled = useSimulatedData,
                        onValueChange = onSwitchesChanged
                    )

                    // Slider 6: Foreground Latency
                    SliderItem(
                        label = "Foreground Latency",
                        value = foregroundLatency,
                        range = 0f..5.0f,
                        valueLabel = String.format("%.1fs", foregroundLatency),
                        enabled = useSimulatedData,
                        onValueChange = onLatencyChanged
                    )

                    // Slider 7: Night ratio
                    SliderItem(
                        label = "Night Session Ratio",
                        value = nightRatio * 100f,
                        range = 0f..100f,
                        valueLabel = String.format("%.0f%%", nightRatio * 100f),
                        enabled = useSimulatedData,
                        onValueChange = { onNightChanged(it / 100f) }
                    )
                }
            }
        }

        // Section B: Data & Privacy
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderSection(title = "Data & Privacy", icon = Icons.Default.Security)
            
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    ToggleRow(
                        title = "Local-Only Storage",
                        subtitle = "Keep all behavioral logs on device.",
                        checked = localStorageOnly,
                        onCheckedChange = { localStorageOnly = it }
                    )
                    Divider(color = HairlineSoft, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ToggleRow(
                        title = "Quiet Hours Lock",
                        subtitle = "Auto-suppress alerts from 10 PM.",
                        checked = quietHours,
                        onCheckedChange = { quietHours = it }
                    )
                }
            }
        }

        // Section C: Passive Monitoring
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderSection(title = "Passive Monitoring", icon = Icons.Default.Visibility)
            
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    SimpleToggleRow(
                        title = "Session Dynamics Monitoring",
                        checked = monitorSession,
                        onCheckedChange = { monitorSession = it }
                    )
                    Divider(color = HairlineSoft, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SimpleToggleRow(
                        title = "Micro-Behaviors Monitoring",
                        checked = monitorMicro,
                        onCheckedChange = { monitorMicro = it }
                    )
                    Divider(color = HairlineSoft, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SimpleToggleRow(
                        title = "Task-Switching Monitoring",
                        checked = monitorSwitch,
                        onCheckedChange = { monitorSwitch = it }
                    )
                }
            }
        }

        // Section C: Diagnostics & Testing
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderSection(title = "Diagnostics & Testing", icon = Icons.Default.Person)
            
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seed Database with Test Data",
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Generate 8 hourly database logs with random API scores for today. This helps verify that the line chart is reading, aggregating, and drawing custom database records correctly.",
                        color = SecondaryGray,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = onSeedTestData,
                        colors = ButtonDefaults.buttonColors(containerColor = CommerceCobalt),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            text = "Seed 8 Hourly Logs",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Trigger Attention Nudge Pop-up",
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "Simulate a late-night scrolling warning pop-up. This will calculate the real scrolling percentage from the database logs and open the Nudge modal directly.",
                        color = SecondaryGray,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onTriggerNudgeModal,
                        colors = ButtonDefaults.buttonColors(containerColor = RiskModerate),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            text = "Trigger Nudge Modal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Attention Guard v1.0 • Privacy-Preserving AI Model",
            color = SecondaryGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun HeaderSection(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = SurfaceSoft,
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = CommerceCobalt,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceDark
        )
    }
}

@Composable
fun SliderItem(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit
) {
    // Sanitize value to prevent NaN crash and ensure it lies within range bounds
    val safeValue = remember(value, range) {
        if (value.isNaN() || value.isInfinite()) {
            range.start
        } else {
            value.coerceIn(range.start, range.endInclusive)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 14.sp, color = if (enabled) OnSurfaceDark else SecondaryGray, fontWeight = FontWeight.Medium)
            Text(text = valueLabel, fontSize = 12.sp, color = if (enabled) CommerceCobalt else SecondaryGray, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = safeValue,
            valueRange = range,
            enabled = enabled,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) InkButton else SecondaryGray.copy(alpha = 0.5f),
                activeTrackColor = if (enabled) InkButton else SecondaryGray.copy(alpha = 0.3f),
                inactiveTrackColor = SurfaceSoft
            )
        )
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
            Text(text = subtitle, fontSize = 11.sp, color = SecondaryGray, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CommerceCobalt, // Cobalt reserved for permissions toggles
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Hairline
            )
        )
    }
}

@Composable
fun SimpleToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurfaceDark)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CommerceCobalt,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Hairline
            )
        )
    }
}
