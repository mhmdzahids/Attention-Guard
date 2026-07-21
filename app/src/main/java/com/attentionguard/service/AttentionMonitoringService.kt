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
        var useSimulatedData = true
        var apiScore = 0.52f
        var riskTier = "moderate"

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
            
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            // 1. Session Duration & Night-time Ratio
            var totalDailyMs = 0L
            var totalNightMs = 0L
            var youtubeMs = 0L
            var instagramMs = 0L
            var tiktokMs = 0L

            val dailyStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            for ((pkg, stat) in dailyStats) {
                if (TARGET_PACKAGES.contains(pkg)) {
                    totalDailyMs += stat.totalTimeInForeground
                }
                when (pkg) {
                    "com.google.android.youtube" -> youtubeMs += stat.totalTimeInForeground
                    "com.instagram.android" -> instagramMs += stat.totalTimeInForeground
                    "com.zhiliaoapp.musically", "com.ss.android.ugc.trill", "com.ss.android.ugc.aweme", "com.ss.android.ugc.aweme.lite" -> tiktokMs += stat.totalTimeInForeground
                }
            }

            val nightCalendar = Calendar.getInstance()
            nightCalendar.set(Calendar.HOUR_OF_DAY, 6)
            nightCalendar.set(Calendar.MINUTE, 0)
            nightCalendar.set(Calendar.SECOND, 0)
            nightCalendar.set(Calendar.MILLISECOND, 0)
            val nightEnd = Math.min(endTime, nightCalendar.timeInMillis)

            if (endTime >= startTime) {
                val nightStats = usageStatsManager.queryAndAggregateUsageStats(startTime, nightEnd)
                for ((pkg, stat) in nightStats) {
                    if (TARGET_PACKAGES.contains(pkg)) {
                        totalNightMs += stat.totalTimeInForeground
                    }
                }
            }

            val sessionHours = totalDailyMs.toFloat() / 3600000f
            val nightRatioVal = if (totalDailyMs > 0L) totalNightMs.toFloat() / totalDailyMs.toFloat() else 0.0f
            
            val youtubeHours = youtubeMs.toFloat() / 3600000f
            val instagramHours = instagramMs.toFloat() / 3600000f
            val tiktokHours = tiktokMs.toFloat() / 3600000f

            // 2. Scroll Velocity
            val scrollVelocityVal = AttentionAccessibilityService.getScrollVelocity()

            // 3. Task Switches per hour
            var switchCount = 0
            try {
                val events = usageStatsManager.queryEvents(startTime, endTime)
                val event = UsageEvents.Event()
                var lastPackage: String? = null
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        val currentPackage = event.packageName
                        if (currentPackage != lastPackage && lastPackage != null) {
                            switchCount++
                        }
                        lastPackage = currentPackage
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying UsageEvents", e)
            }

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

        fun updateCalculations(session: Float, scroll: Float, switches: Float, night: Float, skip: Float) {
            currentSession = session
            currentScroll = scroll
            currentSwitches = switches
            currentNight = night
            currentSkip = skip

            // For simulated mode, distribute session harian dynamically
            if (useSimulatedData) {
                youtubeDuration = session * 0.40f
                instagramDuration = session * 0.30f
                tiktokDuration = session * 0.30f
            }

            // Normalizations (N(x))
            val nSession = Math.min(1.0f, Math.max(0.0f, session / 8.0f))
            val nScroll = Math.min(1.0f, Math.max(0.0f, scroll / 250.0f))
            val nSwitch = Math.min(1.0f, Math.max(0.0f, switches / 20.0f))
            val nNight = Math.min(1.0f, Math.max(0.0f, night))

            // Formula: API = (0.30 * N(session)) + (0.20 * N(scroll)) + (0.30 * N(switch)) + (0.20 * N(night))
            val rawScore = (0.30f * nSession) + (0.20f * nScroll) + (0.30f * nSwitch) + (0.20f * nNight)
            apiScore = Math.round(rawScore * 100f) / 100f

            val prevRisk = riskTier
            riskTier = when {
                apiScore < 0.35f -> "low"
                apiScore < 0.65f -> "moderate"
                else -> "high"
            }

            onMetricsUpdated?.invoke(session, scroll, switches, night, skip, apiScore, riskTier)

            if (riskTier != prevRisk) {
                onTriggerAlert?.invoke(riskTier, apiScore)
            }
        }
    }
}
