package com.attentionguard.service

import android.app.*
import android.app.usage.UsageStatsManager
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
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return
        
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        // Session Dynamics (Total Foreground Time)
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        var totalForegroundMs = 0L
        var switches = 0
        var nightUsageMs = 0L
        
        // Post-midnight calculation bounds (12 AM to 6 AM)
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        if (stats != null) {
            for (stat in stats) {
                totalForegroundMs += stat.totalTimeInForeground
                
                // Count app transitions on newer API levels (mocking launch counts if unavailable)
                switches += 1
                
                if (currentHour in 0..6) {
                    nightUsageMs += stat.totalTimeInForeground
                }
            }
        }

        // Convert foreground to hours
        val sessionHours = totalForegroundMs.toFloat() / (3600000f)
        val scrollSpeed = AttentionAccessibilityService.getScrollVelocity()
        
        // Calculate switch frequency per hour
        val elapsedHrs = (endTime - startTime).toFloat() / 3600000f
        val switchFreqVal = if (elapsedHrs > 0) switches.toFloat() / elapsedHrs else 8.2f
        
        // Night ratio
        val nightRatioVal = if (totalForegroundMs > 0) nightUsageMs.toFloat() / totalForegroundMs.toFloat() else 0.12f

        // Feed metrics to calculate API
        updateCalculations(
            session = Math.min(8.0f, sessionHours),
            scroll = scrollSpeed,
            switches = Math.min(20.0f, switchFreqVal),
            night = Math.min(1.0f, nightRatioVal)
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
        
        // Callback when a risk trigger changes
        var onTriggerAlert: ((risk: String, score: Float) -> Unit)? = null

        fun updateCalculations(session: Float, scroll: Float, switches: Float, night: Float) {
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

            if (riskTier != prevRisk) {
                onTriggerAlert?.invoke(riskTier, apiScore)
            }
        }
    }
}
