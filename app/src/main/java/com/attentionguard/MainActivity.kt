package com.attentionguard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.attentionguard.service.AttentionMonitoringService
import com.attentionguard.ui.screens.*
import com.attentionguard.ui.theme.AttentionGuardTheme
import com.attentionguard.ui.components.NudgeModal
import com.attentionguard.ui.components.PreventionPlanOverlayModal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start passive monitoring service
        val serviceIntent = Intent(this, AttentionMonitoringService::class.java)
        startService(serviceIntent)
        
        setContent {
            AttentionGuardTheme {
                MainAppScaffold()
            }
        }
    }
}

// Data holder for historical alerts
data class AlertLog(
    val id: Long,
    val timestamp: String,
    val title: String,
    val description: String,
    val apiScore: Float,
    val riskTier: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold() {
    var activeTab by remember { mutableStateOf("today") }
    
    // Behaviors State
    var sessionDuration by remember { mutableStateOf(2.5f) }
    var launchFrequency by remember { mutableStateOf(12) }
    var scrollVelocity by remember { mutableStateOf(142f) }
    var skipFrequency by remember { mutableStateOf(64f) }
    var switchFreq by remember { mutableStateOf(8.2f) }
    var foregroundLatency by remember { mutableStateOf(1.5f) }
    var nightRatio by remember { mutableStateOf(0.75f) }

    // Calculated state
    var apiScore by remember { mutableStateOf(0.52f) }
    var riskTier by remember { mutableStateOf("moderate") }

    // Modal state triggers
    var showNudgeModal by remember { mutableStateOf(false) }
    var showOverlayModal by remember { mutableStateOf(false) }

    // Alerts collection
    val alertsList = remember {
        mutableStateListOf(
            AlertLog(1, "06:12 PM Today", "Late-Night Scrolling Detected", "You've spent 40% more time on short-form videos after midnight this week.", 0.52f, "moderate"),
            AlertLog(2, "10:15 AM Yesterday", "High Task-Switching", "Task switching frequency exceeded normal baseline by 35% during work hours.", 0.58f, "moderate"),
            AlertLog(3, "08:30 PM 2 Days Ago", "Sustained Low-Risk State", "Attention Performance Indicator maintained stable cognitive load.", 0.24f, "low")
        )
    }

    // Connect trigger updates from service callback
    LaunchedEffect(Unit) {
        AttentionMonitoringService.onTriggerAlert = { newRisk, newScore ->
            riskTier = newRisk
            apiScore = newScore

            val sdf = SimpleDateFormat("hh:mm a 'Today'", Locale.getDefault())
            val dateStr = sdf.format(Date())

            if (newRisk == "high") {
                alertsList.add(0, AlertLog(
                    id = System.currentTimeMillis(),
                    timestamp = dateStr,
                    title = "High Attention Risk Detected",
                    description = "Severe pattern fragmentation detected across all passive behavior sensors.",
                    apiScore = newScore,
                    riskTier = newRisk
                ))
                showOverlayModal = true
            } else if (newRisk == "moderate") {
                alertsList.add(0, AlertLog(
                    id = System.currentTimeMillis(),
                    timestamp = dateStr,
                    title = "Late-Night Scroll Alert",
                    description = "Interaction micro-behaviors indicate fast skipping under moderate cognitive load.",
                    apiScore = newScore,
                    riskTier = newRisk
                ))
                showNudgeModal = true
            }
        }
    }

    // Function to handle signal changes and update calculations
    val onSignalChanged: () -> Unit = {
        AttentionMonitoringService.updateCalculations(
            session = sessionDuration,
            scroll = scrollVelocity,
            switches = switchFreq,
            night = nightRatio
        )
        apiScore = AttentionMonitoringService.apiScore
        riskTier = AttentionMonitoringService.riskTier
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    TabItem("today", "Today", Icons.Default.CalendarToday),
                    TabItem("insights", "Insights", Icons.Default.Analytics),
                    TabItem("alerts", "Alerts", Icons.Default.NotificationsActive),
                    TabItem("meditate", "Meditate", Icons.Default.SelfImprovement),
                    TabItem("settings", "Profile", Icons.Default.Person)
                )
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab.id,
                        onClick = { activeTab = tab.id },
                        label = { Text(tab.label) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0064E0),
                            selectedTextColor = Color(0xFF0064E0),
                            unselectedIconColor = Color(0xFF5E5E5E),
                            unselectedTextColor = Color(0xFF5E5E5E),
                            indicatorColor = Color(0xFFF1F4F7)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                "today" -> DashboardScreen(
                    apiScore = apiScore,
                    riskTier = riskTier,
                    sessionDuration = sessionDuration,
                    scrollVelocity = scrollVelocity,
                    switchFreq = switchFreq,
                    nightRatio = nightRatio
                )
                "insights" -> InsightsScreen(
                    apiScore = apiScore,
                    riskTier = riskTier,
                    sessionDuration = sessionDuration,
                    scrollVelocity = scrollVelocity,
                    switchFreq = switchFreq,
                    nightRatio = nightRatio,
                    skipFrequency = skipFrequency,
                    onNavigateToMeditate = { activeTab = "meditate" }
                )
                "alerts" -> AlertsScreen(alerts = alertsList)
                "meditate" -> MeditateScreen(
                    apiScore = apiScore,
                    riskTier = riskTier,
                    onActivatePlan = { /* Action callback */ }
                )
                "settings" -> SettingsScreen(
                    sessionDuration = sessionDuration,
                    onSessionChanged = { sessionDuration = it; onSignalChanged() },
                    launchFrequency = launchFrequency,
                    onLaunchesChanged = { launchFrequency = it; onSignalChanged() },
                    scrollVelocity = scrollVelocity,
                    onScrollChanged = { scrollVelocity = it; onSignalChanged() },
                    skipFrequency = skipFrequency,
                    onSkipsChanged = { skipFrequency = it; onSignalChanged() },
                    switchFreq = switchFreq,
                    onSwitchesChanged = { switchFreq = it; onSignalChanged() },
                    foregroundLatency = foregroundLatency,
                    onLatencyChanged = { foregroundLatency = it; onSignalChanged() },
                    nightRatio = nightRatio,
                    onNightChanged = { nightRatio = it; onSignalChanged() }
                )
            }

            // Moderate Risk Dialog Popup
            if (showNudgeModal) {
                NudgeModal(
                    apiScore = apiScore,
                    onDismiss = { showNudgeModal = false }
                )
            }

            // High Risk Fullscreen Overlay
            if (showOverlayModal) {
                PreventionPlanOverlayModal(
                    apiScore = apiScore,
                    onDismiss = { showOverlayModal = false },
                    onActivate = {
                        showOverlayModal = false
                        activeTab = "meditate"
                    }
                )
            }
        }
    }
}

data class TabItem(val id: String, val label: String, val icon: ImageVector)
