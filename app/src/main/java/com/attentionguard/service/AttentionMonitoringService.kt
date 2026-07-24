package com.attentionguard.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Calendar
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import com.attentionguard.data.AppDatabase
import com.attentionguard.data.AttentionLog

class AttentionMonitoringService : Service() {

    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Attention Guard Active")
                .setContentText("Passive attention monitoring is running.")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
            
            if (Build.VERSION.SDK_INT >= 34) { // Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isRunning = true
            startMonitoringLoop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoringLoop() {
        // Monitoring loop to query UsageStatsManager in real Android environment
        Thread {
            while (isRunning) {
                try {
                    checkAndInsertDecayLog(this)
                    
                    if (!useSimulatedData) {
                        querySystemMetrics()
                    }
                    Thread.sleep(15000) // check every 15s
                } catch (e: InterruptedException) {
                    break
                }
            }
        }.start()
    }

    private fun querySystemMetrics() {
        val results = queryMetricsDirectly(this)
        
        // Update the app-specific durations in companion
        youtubeDuration = results.youtube
        instagramDuration = results.instagram
        tiktokDuration = results.tiktok

        // Feed metrics to calculate API
        updateCalculations(
            context = this,
            session = Math.min(8.0f, results.session),
            scroll = results.scroll,
            switches = Math.min(20.0f, results.switches),
            night = Math.min(1.0f, results.night),
            skip = results.skip
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Passive Attention Monitors",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    companion object {
        private const val TAG = "AttentionMonitorService"
        private const val CHANNEL_ID = "attention_monitor_channel"
        private const val NOTIFICATION_ID = 101

        // Active State
        var useSimulatedData = false
        var apiScore = 0.52f
        var riskTier = "moderate"
        private var lastAlertTime = 0L
        private var lastAlertRisk: String? = null
        private var lastCalculationDay = -1

        private fun checkAndResetAtMidnight() {
            val currentDay = ZonedDateTime.now(ZoneId.systemDefault()).dayOfYear
            if (lastCalculationDay != -1 && currentDay != lastCalculationDay) {
                currentSession = 0f
                currentScroll = 0f
                currentSwitches = 0f
                currentNight = 0f
                currentSkip = 0f
                youtubeDuration = 0f
                instagramDuration = 0f
                tiktokDuration = 0f
                apiScore = 0f
                riskTier = "low"
            }
            lastCalculationDay = currentDay
        }

        fun checkAndInsertDecayLog(context: Context) {
            try {
                val db = AppDatabase.getDatabase(context)
                kotlinx.coroutines.runBlocking {
                    val lastLog = db.attentionLogDao().getLatestLog()
                    val now = System.currentTimeMillis()
                    if (lastLog != null) {
                        val diffMs = now - lastLog.timestamp
                        val fifteenMinutesMs = 15 * 60 * 1000L
                        
                        val activePkg = AttentionAccessibilityService.activePackage
                        val isShortFormRunning = activePkg != null && TARGET_PACKAGES.contains(activePkg)
                        
                        if (diffMs > fifteenMinutesMs && !isShortFormRunning) {
                            Log.d(TAG, "Inserting decay log. Time difference: ${diffMs / 60000} mins. No active short-form app.")
                            
                            val decayLog = AttentionLog(
                                timestamp = now,
                                sessionDuration = lastLog.sessionDuration,
                                scrollVelocity = 0f,
                                taskSwitches = 0f,
                                nightRatio = 0f,
                                apiScore = 0.0f,
                                riskTier = "low",
                                isAlertEvent = false
                            )
                            db.attentionLogDao().insertLog(decayLog)
                            
                            if (!useSimulatedData) {
                                checkAndResetAtMidnight()
                                apiScore = 0.0f
                                riskTier = "low"
                                currentScroll = 0f
                                currentSwitches = 0f
                                currentNight = 0f
                                onMetricsUpdated?.invoke(
                                    currentSession,
                                    currentScroll,
                                    currentSwitches,
                                    currentNight,
                                    currentSkip,
                                    apiScore,
                                    riskTier
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking/inserting decay log", e)
            }
        }

        // Current real-world or simulated metric values
        var currentSession = 2.5f
        var currentScroll = 142f
        var currentSwitches = 8.2f
        var currentNight = 0.12f
        var currentSkip = 64f

        // App-specific exposure durations (in hours)
        var youtubeDuration = 1.0f
        var instagramDuration = 0.8f
        var tiktokDuration = 0.7f

        // App installation status flags
        var isYoutubeInstalled = true
        var isInstagramInstalled = true
        var isTiktokInstalled = true
        
        // Callback when a risk trigger changes
        var onTriggerAlert: ((risk: String, score: Float) -> Unit)? = null

        // Callback when any metric or score is updated
        var onMetricsUpdated: ((session: Float, scroll: Float, switches: Float, night: Float, skip: Float, score: Float, risk: String) -> Unit)? = null

        // Data holder for direct query output
        data class MetricResults(
            val session: Float,
            val scroll: Float,
            val switches: Float,
            val night: Float,
            val youtube: Float,
            val instagram: Float,
            val tiktok: Float,
            val skip: Float
        )

        val TARGET_PACKAGES = setOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.ss.android.ugc.aweme",
            "com.ss.android.ugc.aweme.lite"
        )

        fun getCanonicalPackageName(pkg: String): String {
            return when (pkg) {
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
                "com.ss.android.ugc.aweme",
                "com.ss.android.ugc.aweme.lite" -> "com.zhiliaoapp.musically"
                else -> pkg
            }
        }

        private fun isPackageInstalled(context: Context, packageName: String): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
        }

        fun checkInstallationStatus(context: Context) {
            if (useSimulatedData) {
                isYoutubeInstalled = true
                isInstagramInstalled = true
                isTiktokInstalled = true
            } else {
                isYoutubeInstalled = isPackageInstalled(context, "com.google.android.youtube")
                isInstagramInstalled = isPackageInstalled(context, "com.instagram.android")
                isTiktokInstalled = isPackageInstalled(context, "com.zhiliaoapp.musically") || 
                                     isPackageInstalled(context, "com.ss.android.ugc.trill") ||
                                     isPackageInstalled(context, "com.ss.android.ugc.aweme") ||
                                     isPackageInstalled(context, "com.ss.android.ugc.aweme.lite")
            }
        }

        fun queryMetricsDirectly(context: Context): MetricResults {
            checkInstallationStatus(context)
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return MetricResults(1.5f, 142f, 8.2f, 0.12f, 0.6f, 0.5f, 0.4f, 64f)
            
            val endTime = System.currentTimeMillis()
            val startTime = ZonedDateTime.now(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant()
                .toEpochMilli()

            val maxPossibleMs = Math.max(0L, endTime - startTime)

            // 1. Get exact durations from aggregated UsageStats (matches Digital Wellbeing)
            var totalDailyMs = 0L
            var youtubeMs = 0L
            var instagramMs = 0L
            var tiktokMs = 0L

            val dailyStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            for ((pkg, stat) in dailyStats) {
                val canonicalPkg = getCanonicalPackageName(pkg)
                val timeInFg = Math.min(stat.totalTimeInForeground, maxPossibleMs)
                if (timeInFg > 0L) {
                    when (canonicalPkg) {
                        "com.google.android.youtube" -> youtubeMs += timeInFg
                        "com.instagram.android" -> instagramMs += timeInFg
                        "com.zhiliaoapp.musically" -> tiktokMs += timeInFg
                    }
                    if (TARGET_PACKAGES.contains(pkg) || TARGET_PACKAGES.contains(canonicalPkg)) {
                        totalDailyMs += timeInFg
                    }
                }
            }

            // 2. Query UsageEvents solely for switches and night ratio
            var totalNightMs = 0L
            var switchCount = 0
            
            try {
                val events = usageStatsManager.queryEvents(startTime, endTime)
                val event = UsageEvents.Event()
                val appOpenTimes = mutableMapOf<String, Long>()
                var lastResumedPackage: String? = null
                var lastPackage: String? = null
                
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    val pkg = event.packageName ?: continue
                    val time = event.timeStamp
                    val canonicalPkg = getCanonicalPackageName(pkg)
                    
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        appOpenTimes[canonicalPkg] = time
                        lastResumedPackage = canonicalPkg
                        
                        if (canonicalPkg != lastPackage && lastPackage != null) {
                            switchCount++
                        }
                        lastPackage = canonicalPkg
                    } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED || 
                               event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                        val openTime = appOpenTimes.remove(canonicalPkg)
                        if (openTime != null && time > openTime) {
                            val duration = time - openTime
                            if (TARGET_PACKAGES.contains(pkg) || TARGET_PACKAGES.contains(canonicalPkg)) {
                                val cal = Calendar.getInstance().apply { timeInMillis = openTime }
                                val hr = cal.get(Calendar.HOUR_OF_DAY)
                                if (hr in 0..5) {
                                    totalNightMs += duration
                                }
                            }
                        }
                        if (canonicalPkg == lastResumedPackage) {
                            lastResumedPackage = null
                        }
                    } else if (event.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE || 
                               event.eventType == UsageEvents.Event.KEYGUARD_SHOWN) {
                        lastResumedPackage = null
                    }
                }
                
                // Account for any app still running in foreground for totalNightMs
                for ((pkg, openTime) in appOpenTimes) {
                    val canonicalLastResumed = lastResumedPackage
                    val canonicalActiveAccessibility = AttentionAccessibilityService.activePackage?.let { getCanonicalPackageName(it) }
                    val isCurrentlyForeground = pkg == canonicalLastResumed || pkg == canonicalActiveAccessibility
                    
                    if (isCurrentlyForeground && endTime > openTime) {
                        val duration = endTime - openTime
                        val cal = Calendar.getInstance().apply { timeInMillis = openTime }
                        val hr = cal.get(Calendar.HOUR_OF_DAY)
                        if (hr in 0..5) {
                            totalNightMs += duration
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating night ratio / switches in queryMetricsDirectly", e)
            }

            val sessionHours = totalDailyMs.toFloat() / 3600000f
            val nightRatioVal = if (totalDailyMs > 0L) totalNightMs.toFloat() / totalDailyMs.toFloat() else 0.0f
            
            val youtubeHours = youtubeMs.toFloat() / 3600000f
            val instagramHours = instagramMs.toFloat() / 3600000f
            val tiktokHours = tiktokMs.toFloat() / 3600000f

            // 2. Scroll Velocity
            val scrollVelocityVal = AttentionAccessibilityService.getScrollVelocity()

            // 3. Task Switches per hour
            val elapsedHrs = (endTime - startTime).toFloat() / 3600000f
            val switchFreqVal = if (elapsedHrs > 0.05f) switchCount.toFloat() / elapsedHrs else 0f

            return MetricResults(
                session = sessionHours,
                scroll = scrollVelocityVal,
                switches = switchFreqVal,
                night = nightRatioVal,
                youtube = youtubeHours,
                instagram = instagramHours,
                tiktok = tiktokHours,
                skip = AttentionAccessibilityService.getSkipRate()
            )
        }

        fun updateCalculations(context: Context, session: Float, scroll: Float, switches: Float, night: Float, skip: Float) {
            checkAndResetAtMidnight()
            val safeSession = if (session.isNaN() || session.isInfinite()) 0f else session
            val safeScroll = if (scroll.isNaN() || scroll.isInfinite()) 142f else scroll
            val safeSwitches = if (switches.isNaN() || switches.isInfinite()) 0f else switches
            val safeNight = if (night.isNaN() || night.isInfinite()) 0f else night
            val safeSkip = if (skip.isNaN() || skip.isInfinite()) 0f else skip

            currentSession = safeSession
            currentScroll = safeScroll
            currentSwitches = safeSwitches
            currentNight = safeNight
            currentSkip = safeSkip

            // For simulated mode, distribute session harian dynamically
            if (useSimulatedData) {
                youtubeDuration = safeSession * 0.40f
                instagramDuration = safeSession * 0.30f
                tiktokDuration = safeSession * 0.30f
            }

            // Normalizations (N(x))
            val nSession = Math.min(1.0f, Math.max(0.0f, safeSession / 8.0f))
            val nScroll = Math.min(1.0f, Math.max(0.0f, safeScroll / 250.0f))
            val nSwitch = Math.min(1.0f, Math.max(0.0f, safeSwitches / 20.0f))
            val nNight = Math.min(1.0f, Math.max(0.0f, safeNight))

            // Formula: API = (0.30 * N(session)) + (0.20 * N(scroll)) + (0.30 * N(switch)) + (0.20 * N(night))
            val rawScore = (0.30f * nSession) + (0.20f * nScroll) + (0.30f * nSwitch) + (0.20f * nNight)
            val roundedScore = Math.round(rawScore * 100f) / 100f
            apiScore = if (roundedScore.isNaN() || roundedScore.isInfinite()) 0f else roundedScore

            val prevRisk = riskTier
            riskTier = when {
                apiScore < 0.35f -> "low"
                apiScore < 0.65f -> "moderate"
                else -> "high"
            }

            // Implement a 10-minute cooldown on alert triggers to prevent flip-flop/jitter spam
            val now = System.currentTimeMillis()
            var isGenuineAlertTransition = false
            if (riskTier != prevRisk && riskTier != "low") {
                val cooldownMs = 600000L // 10 minutes cooldown
                if (riskTier != lastAlertRisk || (now - lastAlertTime) > cooldownMs) {
                    isGenuineAlertTransition = true
                    lastAlertTime = now
                    lastAlertRisk = riskTier
                }
            }

            val appContext = context.applicationContext
            Thread {
                try {
                    val db = AppDatabase.getDatabase(appContext)
                    kotlinx.coroutines.runBlocking {
                        val shouldLog = if (useSimulatedData) {
                            true
                        } else {
                            val lastLog = db.attentionLogDao().getLatestLog()
                            val lastLogTime = lastLog?.timestamp ?: 0L
                            // Write immediately if it is a genuine new alert, otherwise rate limit to 1 minute
                            isGenuineAlertTransition || (now - lastLogTime) > 60000L
                        }

                        if (shouldLog) {
                            val log = AttentionLog(
                                timestamp = now,
                                sessionDuration = safeSession,
                                scrollVelocity = safeScroll,
                                taskSwitches = safeSwitches,
                                nightRatio = safeNight,
                                apiScore = apiScore,
                                riskTier = riskTier,
                                isAlertEvent = isGenuineAlertTransition
                            )
                            db.attentionLogDao().insertLog(log)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AttentionMonitor", "Error writing metrics to database", e)
                }
            }.start()

            onMetricsUpdated?.invoke(safeSession, safeScroll, safeSwitches, safeNight, safeSkip, apiScore, riskTier)

            // Trigger visual modal notifications only when cooldown conditions are met
            if (isGenuineAlertTransition) {
                onTriggerAlert?.invoke(riskTier, apiScore)
                showRiskAlertNotification(context, riskTier, apiScore)
            }
        }

        private fun showRiskAlertNotification(context: Context, riskLevel: String, apiScore: Float) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val alertChannelId = "attention_risk_alerts"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    alertChannelId,
                    "Attention Risk Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Delivers immediate behavioral nudges and attention risk alerts"
                }
                manager.createNotificationChannel(channel)
            }
            
            val intent = Intent(context, Class.forName("com.attentionguard.MainActivity")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("risk_alert", riskLevel)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                riskLevel.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val title = if (riskLevel == "high") "High Attention Fatigue" else "Attention Drift Detected"
            val body = if (riskLevel == "high") {
                "Compulsive scrolling detected. Tap to launch your Prevention Plan."
            } else {
                "You've been in a continuous short-form video session. Take a 5-minute break."
            }
            
            val notification = NotificationCompat.Builder(context, alertChannelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
                
            manager.notify(riskLevel.hashCode(), notification)
        }

        data class HourlyBucketData(
            val hour: Int,
            val label: String,
            val startTimeMs: Long,
            val endTimeMs: Long,
            val durationMs: Long,
            val apiScore: Float
        )

        fun queryHourlyBuckets(context: Context): List<HourlyBucketData> {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return emptyList()

            val zoneId = ZoneId.systemDefault()
            val nowZdt = ZonedDateTime.now(zoneId)
            val startOfDayZdt = nowZdt.truncatedTo(ChronoUnit.DAYS)
            val currentHour = nowZdt.hour
            val nowMs = System.currentTimeMillis()

            val buckets = mutableListOf<HourlyBucketData>()

            for (h in 0..23) {
                val bucketStart = startOfDayZdt.plusHours(h.toLong())
                val bucketEnd = bucketStart.plusHours(1)
                val startTimeMs = bucketStart.toInstant().toEpochMilli()
                val endTimeMs = bucketEnd.toInstant().toEpochMilli()

                val amPm = if (h >= 12) "PM" else "AM"
                val displayHour = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }
                val label = String.format("%02d:00 %s", displayHour, amPm)

                if (h > currentHour || startTimeMs > nowMs) {
                    buckets.add(HourlyBucketData(h, label, startTimeMs, endTimeMs, 0L, 0.0f))
                    continue
                }

                val actualEnd = Math.min(endTimeMs, nowMs)
                if (actualEnd <= startTimeMs) {
                    buckets.add(HourlyBucketData(h, label, startTimeMs, endTimeMs, 0L, 0.0f))
                    continue
                }

                var hourlyMs = 0L
                val maxBucketDuration = actualEnd - startTimeMs
                try {
                    val stats = usageStatsManager.queryAndAggregateUsageStats(startTimeMs, actualEnd)
                    for ((pkg, stat) in stats) {
                        val canonicalPkg = getCanonicalPackageName(pkg)
                        if (TARGET_PACKAGES.contains(pkg) || TARGET_PACKAGES.contains(canonicalPkg)) {
                            val timeInFg = Math.min(stat.totalTimeInForeground, maxBucketDuration)
                            if (timeInFg > 0L) {
                                hourlyMs += timeInFg
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error querying hourly stats for hour $h", e)
                }

                val score = if (hourlyMs == 0L) {
                    0.0f
                } else {
                    val hourlySessionHrs = hourlyMs.toFloat() / 3600000f
                    val nSession = Math.min(1.0f, Math.max(0.0f, hourlySessionHrs / 1.0f))
                    val nScroll = Math.min(1.0f, Math.max(0.0f, currentScroll / 250.0f))
                    val nSwitch = Math.min(1.0f, Math.max(0.0f, currentSwitches / 20.0f))
                    val nNight = if (h in 0..5) 1.0f else 0.0f

                    val raw = (0.40f * nSession) + (0.20f * nScroll) + (0.20f * nSwitch) + (0.20f * nNight)
                    val rounded = Math.round(raw * 100f) / 100f
                    Math.min(1.0f, Math.max(0.05f, rounded))
                }

                buckets.add(HourlyBucketData(h, label, startTimeMs, endTimeMs, hourlyMs, score))
            }

            return buckets
        }
    }
}
