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
import androidx.compose.ui.platform.LocalContext
import androidx.work.*
import com.attentionguard.service.AttentionMonitoringService
import com.attentionguard.service.AttentionAccessibilityService
import com.attentionguard.service.AttentionCalculationWorker
import com.attentionguard.ui.screens.*
import com.attentionguard.ui.theme.AttentionGuardTheme
import com.attentionguard.ui.components.NudgeModal
import com.attentionguard.ui.components.PreventionPlanOverlayModal
import com.attentionguard.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start passive monitoring service
        val serviceIntent = Intent(this, AttentionMonitoringService::class.java)
        startService(serviceIntent)

        // Schedule hourly AttentionCalculationWorker
        scheduleHourlyCalculation()
        
        setContent {
            AttentionGuardTheme {
                MainAppScaffold()
            }
        }
    }

    private fun scheduleHourlyCalculation() {
        val workRequest = PeriodicWorkRequestBuilder<AttentionCalculationWorker>(
            1, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AttentionCalculation",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
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
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val dbLogs by database.attentionLogDao().getAllLogsFlow().collectAsState(initial = emptyList())
    
    var useSimulatedData by remember { mutableStateOf(AttentionMonitoringService.useSimulatedData) }
    
    // Behaviors State
    var sessionDuration by remember { mutableStateOf(2.5f) }
    var launchFrequency by remember { mutableStateOf(12) }
    var scrollVelocity by remember { mutableStateOf(142f) }
    var skipFrequency by remember { mutableStateOf(64f) }
    var switchFreq by remember { mutableStateOf(8.2f) }
    var foregroundLatency by remember { mutableStateOf(1.5f) }
    var nightRatio by remember { mutableStateOf(0.75f) }

    // App specific exposure states
    var youtubeDuration by remember { mutableStateOf(1.0f) }
    var instagramDuration by remember { mutableStateOf(0.8f) }
    var tiktokDuration by remember { mutableStateOf(0.7f) }

    var isYoutubeInstalled by remember { mutableStateOf(AttentionMonitoringService.isYoutubeInstalled) }
    var isInstagramInstalled by remember { mutableStateOf(AttentionMonitoringService.isInstagramInstalled) }
    var isTiktokInstalled by remember { mutableStateOf(AttentionMonitoringService.isTiktokInstalled) }

    // Calculated state
    var apiScore by remember { mutableStateOf(0.52f) }
    var riskTier by remember { mutableStateOf("moderate") }

    // Modal state triggers
    var showNudgeModal by remember { mutableStateOf(false) }
    var showOverlayModal by remember { mutableStateOf(false) }

    // Alerts collection mapped dynamically from Database
    val sdfTime = remember { SimpleDateFormat("hh:mm a dd/MM", Locale.getDefault()) }
    val alertsList = remember(dbLogs) {
        if (dbLogs.isEmpty()) {
            listOf(
                AlertLog(1, "06:12 PM Today", "Late-Night Scrolling Detected", "You've spent 40% more time on short-form videos after midnight this week.", 0.52f, "moderate"),
                AlertLog(2, "10:15 AM Yesterday", "High Task-Switching", "Task switching frequency exceeded normal baseline by 35% during work hours.", 0.58f, "moderate"),
                AlertLog(3, "08:30 PM 2 Days Ago", "Sustained Low-Risk State", "Attention Performance Indicator maintained stable cognitive load.", 0.24f, "low")
            )
        } else {
            dbLogs.map { log ->
                val date = Date(log.timestamp)
                val formattedDate = sdfTime.format(date)
                
                val title = when (log.riskTier) {
                    "high" -> "High Attention Risk Detected"
                    "moderate" -> "Late-Night Scroll Alert"
                    else -> "Sustained Low-Risk State"
                }
                val desc = when (log.riskTier) {
                    "high" -> "Severe pattern fragmentation detected across all passive behavior sensors."
                    "moderate" -> "Interaction micro-behaviors indicate fast skipping under moderate cognitive load."
                    else -> "Attention Performance Indicator maintained stable cognitive load."
                }
                
                AlertLog(
                    id = log.id,
                    timestamp = formattedDate,
                    title = title,
                    description = desc,
                    apiScore = log.apiScore,
                    riskTier = log.riskTier
                )
            }
        }
    }

    // Connect trigger updates from service callback
    LaunchedEffect(Unit) {
        AttentionMonitoringService.onMetricsUpdated = { session, scroll, switches, night, skip, score, risk ->
            if (!AttentionMonitoringService.useSimulatedData) {
                sessionDuration = session
                scrollVelocity = scroll
                switchFreq = switches
                nightRatio = night
                skipFrequency = skip
                apiScore = score
                riskTier = risk
                
                youtubeDuration = AttentionMonitoringService.youtubeDuration
                instagramDuration = AttentionMonitoringService.instagramDuration
                tiktokDuration = AttentionMonitoringService.tiktokDuration
                
                isYoutubeInstalled = AttentionMonitoringService.isYoutubeInstalled
                isInstagramInstalled = AttentionMonitoringService.isInstagramInstalled
                isTiktokInstalled = AttentionMonitoringService.isTiktokInstalled
            }
        }

        AttentionMonitoringService.onTriggerAlert = { newRisk, newScore ->
            if (newRisk == "high") {
                showOverlayModal = true
            } else if (newRisk == "moderate") {
                showNudgeModal = true
            }
        }
    }

    // Sync initial service states when mode changes
    LaunchedEffect(useSimulatedData) {
        if (!useSimulatedData) {
            sessionDuration = AttentionMonitoringService.currentSession
            scrollVelocity = AttentionMonitoringService.currentScroll
            switchFreq = AttentionMonitoringService.currentSwitches
            nightRatio = AttentionMonitoringService.currentNight
            skipFrequency = AttentionMonitoringService.currentSkip
            apiScore = AttentionMonitoringService.apiScore
            riskTier = AttentionMonitoringService.riskTier
            
            youtubeDuration = AttentionMonitoringService.youtubeDuration
            instagramDuration = AttentionMonitoringService.instagramDuration
            tiktokDuration = AttentionMonitoringService.tiktokDuration
            
            isYoutubeInstalled = AttentionMonitoringService.isYoutubeInstalled
            isInstagramInstalled = AttentionMonitoringService.isInstagramInstalled
            isTiktokInstalled = AttentionMonitoringService.isTiktokInstalled
        } else {
            youtubeDuration = sessionDuration * 0.40f
            instagramDuration = sessionDuration * 0.30f
            tiktokDuration = sessionDuration * 0.30f
            
            isYoutubeInstalled = true
            isInstagramInstalled = true
            isTiktokInstalled = true
        }
    }

    // Function to handle signal changes and update calculations in simulated mode
    val onSignalChanged: () -> Unit = {
        AttentionMonitoringService.updateCalculations(
            context = context,
            session = sessionDuration,
            scroll = scrollVelocity,
            switches = switchFreq,
            night = nightRatio,
            skip = skipFrequency
        )
        apiScore = AttentionMonitoringService.apiScore
        riskTier = AttentionMonitoringService.riskTier
        
        youtubeDuration = AttentionMonitoringService.youtubeDuration
        instagramDuration = AttentionMonitoringService.instagramDuration
        tiktokDuration = AttentionMonitoringService.tiktokDuration
        
        isYoutubeInstalled = true
        isInstagramInstalled = true
        isTiktokInstalled = true
    }

    val onSeedTestData: () -> Unit = {
        val database = AppDatabase.getDatabase(context)
        // Using main scope or lifecycleScope if accessible
        (context as? ComponentActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            database.attentionLogDao().deleteLogsAfter(todayStart)

            val cal = Calendar.getInstance()
            val hours = listOf(0, 3, 6, 9, 12, 15, 18, 21)
            val randomScores = listOf(0.12f, 0.35f, 0.22f, 0.68f, 0.45f, 0.78f, 0.55f, 0.32f)
            val randomSessions = listOf(0.5f, 1.2f, 0.8f, 3.2f, 2.1f, 4.5f, 3.0f, 1.5f)

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            for (i in hours.indices) {
                if (hours[i] <= currentHour) {
                    cal.set(Calendar.HOUR_OF_DAY, hours[i])
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    
                    val log = com.attentionguard.data.AttentionLog(
                        timestamp = cal.timeInMillis,
                        sessionDuration = randomSessions[i],
                        scrollVelocity = 150f,
                        taskSwitches = 5f,
                        nightRatio = 0.1f,
                        apiScore = randomScores[i],
                        riskTier = if (randomScores[i] < 0.35f) "low" else if (randomScores[i] < 0.65f) "moderate" else "high"
                    )
                    database.attentionLogDao().insertLog(log)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .background(Color.White)
                    .drawBehind {
                        drawLine(
                            color = com.attentionguard.ui.theme.HairlineSoft,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2f
                        )
                    }
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val tabs = listOf(
                    TabItem("today", "Today", Icons.Default.CalendarToday),
                    TabItem("insights", "Insights", Icons.Default.BarChart),
                    TabItem("alerts", "Alerts", Icons.Default.Notifications),
                    TabItem("meditate", "Meditate", Icons.Default.SelfImprovement),
                    TabItem("settings", "Profile", Icons.Default.Person)
                )

                tabs.forEach { tab ->
                    val selected = activeTab == tab.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (selected) com.attentionguard.ui.theme.SurfaceSoft else Color.Transparent)
                            .clickable { activeTab = tab.id }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (selected) com.attentionguard.ui.theme.CommerceCobalt else com.attentionguard.ui.theme.SecondaryGray,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) com.attentionguard.ui.theme.CommerceCobalt else com.attentionguard.ui.theme.SecondaryGray
                            )
                        }
                    }
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
                    youtubeDuration = youtubeDuration,
                    instagramDuration = instagramDuration,
                    tiktokDuration = tiktokDuration,
                    isYoutubeInstalled = isYoutubeInstalled,
                    isInstagramInstalled = isInstagramInstalled,
                    isTiktokInstalled = isTiktokInstalled,
                    dbLogs = dbLogs,
                    useSimulatedData = useSimulatedData,
                    onNavigateToMeditate = { activeTab = "meditate" }
                )
                "alerts" -> AlertsScreen(alerts = alertsList)
                "meditate" -> MeditateScreen(
                    apiScore = apiScore,
                    riskTier = riskTier,
                    onActivatePlan = { /* Action callback */ }
                )
                "settings" -> SettingsScreen(
                    useSimulatedData = useSimulatedData,
                    onSimulatedDataToggled = {
                        AttentionMonitoringService.useSimulatedData = it
                        useSimulatedData = it
                        if (!it) {
                            val results = AttentionMonitoringService.queryMetricsDirectly(context)
                            
                            AttentionMonitoringService.youtubeDuration = results.youtube
                            AttentionMonitoringService.instagramDuration = results.instagram
                            AttentionMonitoringService.tiktokDuration = results.tiktok
                            
                            isYoutubeInstalled = AttentionMonitoringService.isYoutubeInstalled
                            isInstagramInstalled = AttentionMonitoringService.isInstagramInstalled
                            isTiktokInstalled = AttentionMonitoringService.isTiktokInstalled

                            AttentionMonitoringService.updateCalculations(
                                context = context,
                                session = results.session,
                                scroll = results.scroll,
                                switches = results.switches,
                                night = results.night,
                                skip = results.skip
                            )
                        }
                    },
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
                    onNightChanged = { nightRatio = it; onSignalChanged() },
                    onSeedTestData = onSeedTestData
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

