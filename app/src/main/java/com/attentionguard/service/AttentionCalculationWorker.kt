package com.attentionguard.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.attentionguard.data.AppDatabase
import com.attentionguard.data.AttentionLog

class AttentionCalculationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing scheduled hourly Attention calculation...")
        
        try {
            // 1. Query metrics directly using the helper method in AttentionMonitoringService
            val results = AttentionMonitoringService.queryMetricsDirectly(applicationContext)
            
            val session = results.session
            val scroll = results.scroll
            val switches = results.switches
            val night = results.night

            // 2. Perform normalizations
            val nSession = Math.min(1.0f, Math.max(0.0f, session / 8.0f))
            val nScroll = Math.min(1.0f, Math.max(0.0f, scroll / 250.0f))
            val nSwitch = Math.min(1.0f, Math.max(0.0f, switches / 20.0f))
            val nNight = Math.min(1.0f, Math.max(0.0f, night))

            // 3. Compute API Score
            // Formula: API = (0.30 * N(session)) + (0.20 * N(scroll)) + (0.30 * N(switch)) + (0.20 * N(night))
            val rawScore = (0.30f * nSession) + (0.20f * nScroll) + (0.30f * nSwitch) + (0.20f * nNight)
            val score = Math.round(rawScore * 100f) / 100f

            val risk = when {
                score < 0.35f -> "low"
                score < 0.65f -> "moderate"
                else -> "high"
            }

            Log.d(TAG, "Calculated real-world API: $score, Risk: $risk")

            // 4. Save to Room database
            val database = AppDatabase.getDatabase(applicationContext)
            val logEntry = AttentionLog(
                timestamp = System.currentTimeMillis(),
                sessionDuration = session,
                scrollVelocity = scroll,
                taskSwitches = switches,
                nightRatio = night,
                apiScore = score,
                riskTier = risk
            )
            database.attentionLogDao().insertLog(logEntry)

            // Store individual app durations in companion variables
            AttentionMonitoringService.youtubeDuration = results.youtube
            AttentionMonitoringService.instagramDuration = results.instagram
            AttentionMonitoringService.tiktokDuration = results.tiktok

            // 5. If Real-world Sensor Mode is enabled, update global calculations so UI reacts
            if (!AttentionMonitoringService.useSimulatedData) {
                // This updates the companion variables and triggers callbacks on the UI
                AttentionMonitoringService.updateCalculations(session, scroll, switches, night)
            }
            
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating attention score in worker", e)
            return Result.failure()
        }
    }

    companion object {
        private const val TAG = "AttentionCalcWorker"
    }
}
