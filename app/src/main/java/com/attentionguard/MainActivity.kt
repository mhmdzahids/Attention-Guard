package com.attentionguard

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
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

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainAppScaffold() {
    val tabs = listOf(
        TabItem("today", "Today", Icons.Default.CalendarToday),
        TabItem("insights", "Insights", Icons.Default.BarChart),
        TabItem("alerts", "Alerts", Icons.Default.Notifications),
        TabItem("meditate", "Meditate", Icons.Default.SelfImprovement),
        TabItem("settings", "Profile", Icons.Default.Person)
    )
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    var transitionTargetPage by remember { mutableStateOf<Int?>(null) }
    var pagerScrollEnabled by remember { mutableStateOf(true) }
    var resetJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    val activePageIndex by remember {
        derivedStateOf { transitionTargetPage ?: pagerState.currentPage }
    }
    
    val customFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    )

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

    var isAccessibilityGranted by remember {
        mutableStateOf(PermissionChecker.isAccessibilityServiceEnabled(context))
    }
    var isOverlayGranted by remember {
        mutableStateOf(PermissionChecker.isOverlayPermissionGranted(context))
    }
    var isNotificationGranted by remember {
        mutableStateOf(PermissionChecker.isNotificationPermissionGranted(context))
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isAccessibilityGranted = PermissionChecker.isAccessibilityServiceEnabled(context)
                isOverlayGranted = PermissionChecker.isOverlayPermissionGranted(context)
                isNotificationGranted = PermissionChecker.isNotificationPermissionGranted(context)

                // Check if opened from notification risk alert
                val act = context as? ComponentActivity
                val riskAlert = act?.intent?.getStringExtra("risk_alert")
                if (riskAlert != null) {
                    if (riskAlert == "moderate") {
                        showNudgeModal = true
                    } else if (riskAlert == "high") {
                        showOverlayModal = true
                    }
                    act.intent.removeExtra("risk_alert")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
            val alertEvents = dbLogs.filter { it.isAlertEvent }
            
            alertEvents.map { log ->
                val date = Date(log.timestamp)
                val formattedDate = sdfTime.format(date)
                
                // Estimate dominant sensor signal contributing to the score
                val contribSession = 0.30f * (log.sessionDuration / 8.0f).coerceIn(0f, 1f)
                val contribScroll = 0.20f * (log.scrollVelocity / 250.0f).coerceIn(0f, 1f)
                val contribSwitch = 0.30f * (log.taskSwitches / 20.0f).coerceIn(0f, 1f)
                val contribNight = 0.20f * log.nightRatio.coerceIn(0f, 1f)
                
                val dominantSignal = listOf(
                    "session" to contribSession,
                    "scroll" to contribScroll,
                    "switch" to contribSwitch,
                    "night" to contribNight
                ).maxByOrNull { it.second }?.first ?: "session"
                
                val title = if (log.riskTier == "high") {
                    when (dominantSignal) {
                        "night" -> "Severe Late-Night Doomscrolling"
                        "switch" -> "Critical Task Fragmentation"
                        "scroll" -> "Frantic Content Doomscrolling"
                        else -> "Excessive App Exposure"
                    }
                } else {
                    when (dominantSignal) {
                        "night" -> "Late-Night Scroll Alert"
                        "switch" -> "High Task-Switching Alert"
                        "scroll" -> "Frantic Scroll Speed Alert"
                        else -> "Prolonged Usage Alert"
                    }
                }
                
                val desc = if (log.riskTier == "high") {
                    when (dominantSignal) {
                        "night" -> "Critical usage levels past midnight indicates high risk of sleep deprivation."
                        "switch" -> "Severe application multitasking is causing extreme cognitive attention breakdown."
                        "scroll" -> "Extremely high scroll velocity and content skip rate indicates severe dopamine seeking."
                        else -> "Daily screen time for short-form content apps has reached dangerous thresholds."
                    }
                } else {
                    when (dominantSignal) {
                        "night" -> "High usage detected past midnight, disrupting healthy sleep patterns."
                        "switch" -> "Frequent switching between apps indicates scattered attention and cognitive fatigue."
                        "scroll" -> "Rapid and irregular scrolling velocity indicates search for instant dopamine gratification."
                        else -> "Targeted app session duration has exceeded healthy daily wellbeing thresholds."
                    }
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

    LaunchedEffect(useSimulatedData) {
        if (useSimulatedData) {
            onSignalChanged()
        }
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

    if (!useSimulatedData && (!isAccessibilityGranted || !isOverlayGranted || !isNotificationGranted)) {
        PermissionSetupScreen(
            isAccessibilityGranted = isAccessibilityGranted,
            isOverlayGranted = isOverlayGranted,
            isNotificationGranted = isNotificationGranted,
            onFixAccessibility = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "Please turn on 'Attention Guard' in Accessibility settings",
                    Toast.LENGTH_LONG
                ).show()
            },
            onFixOverlay = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            },
            onFixNotifications = {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    val act = context as? ComponentActivity
                    act?.requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        102
                    )
                }
            },
            onRemindMeLater = {
                AttentionMonitoringService.useSimulatedData = true
                useSimulatedData = true
                Toast.makeText(
                    context,
                    "Simulated Data mode enabled.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    } else {
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


                    tabs.forEachIndexed { pageIndex, tab ->
                        val selected = activePageIndex == pageIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (selected) com.attentionguard.ui.theme.SurfaceSoft else Color.Transparent)
                                .clickable {
                                    coroutineScope.launch {
                                        try {
                                            transitionTargetPage = pageIndex
                                            pagerState.animateScrollToPage(pageIndex)
                                        } finally {
                                            transitionTargetPage = null
                                        }
                                    }
                                }
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
            HorizontalPager(
                state = pagerState,
                flingBehavior = customFlingBehavior,
                userScrollEnabled = pagerScrollEnabled,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                resetJob?.cancel()
                                var totalX = 0f
                                var totalY = 0f
                                var isDirectionDecided = false
                                
                                do {
                                    val event = awaitPointerEvent()
                                    val dragChange = event.changes.firstOrNull()
                                    if (dragChange != null && dragChange.pressed) {
                                        val positionChange = dragChange.position - dragChange.previousPosition
                                        totalX += positionChange.x
                                        totalY += positionChange.y
                                        
                                        if (!isDirectionDecided && (Math.abs(totalX) > 10f || Math.abs(totalY) > 10f)) {
                                            isDirectionDecided = true
                                            if (Math.abs(totalY) > Math.abs(totalX) * 1.2f) {
                                                pagerScrollEnabled = false
                                            } else {
                                                pagerScrollEnabled = true
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })
                                
                                resetJob = coroutineScope.launch {
                                    kotlinx.coroutines.delay(500)
                                    pagerScrollEnabled = true
                                }
                            }
                        }
                    }
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            clip = true
                        }
                ) {
                    when (page) {
                        0 -> DashboardScreen(
                            apiScore = apiScore,
                            riskTier = riskTier,
                            sessionDuration = sessionDuration,
                            scrollVelocity = scrollVelocity,
                            switchFreq = switchFreq,
                            nightRatio = nightRatio
                        )
                        1 -> InsightsScreen(
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
                            onNavigateToMeditate = {
                                coroutineScope.launch {
                                    val targetIndex = tabs.indexOfFirst { it.id == "meditate" }
                                    if (targetIndex != -1) {
                                        try {
                                            transitionTargetPage = targetIndex
                                            pagerState.animateScrollToPage(targetIndex)
                                        } finally {
                                            transitionTargetPage = null
                                        }
                                    }
                                }
                            },
                            onScrollEnabledChanged = { enabled ->
                                pagerScrollEnabled = enabled
                            }
                        )
                        2 -> AlertsScreen(alerts = alertsList)
                        3 -> MeditateScreen(
                            apiScore = apiScore,
                            riskTier = riskTier,
                            onActivatePlan = { /* Action callback */ }
                        )
                    4 -> SettingsScreen(
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
            }
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
                            coroutineScope.launch {
                                val targetIndex = tabs.indexOfFirst { it.id == "meditate" }
                                if (targetIndex != -1) {
                                    try {
                                        transitionTargetPage = targetIndex
                                        pagerState.animateScrollToPage(targetIndex)
                                    } finally {
                                        transitionTargetPage = null
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

data class TabItem(val id: String, val label: String, val icon: ImageVector)


