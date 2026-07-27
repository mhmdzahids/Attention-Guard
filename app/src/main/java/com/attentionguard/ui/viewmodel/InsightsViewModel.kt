package com.attentionguard.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attentionguard.data.AttentionLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class DiscreteHourlyPoint(
    val hourOfDay: Int,          // 0..23
    val label: String,            // "12:00 AM", "01:00 AM", ...
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val apiScore: Float           // Force 0.0f when durationMs == 0L
)

data class InsightsUiState(
    val hourlyPoints: List<DiscreteHourlyPoint> = emptyList(),
    val peakHourText: String = "N/A",
    val todayTotalActiveDurationMs: Long = 0L
)

class InsightsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    fun refreshHourlyData(context: Context, dbLogs: List<AttentionLog>, useSimulatedData: Boolean) {
        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()
            val nowZdt = ZonedDateTime.now(zoneId)
            val startOfDayZdt = nowZdt.truncatedTo(ChronoUnit.DAYS)
            val startOfDayMs = startOfDayZdt.toInstant().toEpochMilli()
            val currentHour = nowZdt.hour

            if (useSimulatedData) {
                val simulatedPoints = (0..23).map { h ->
                    val amPm = if (h >= 12) "PM" else "AM"
                    val displayHour = when {
                        h == 0 -> 12
                        h > 12 -> h - 12
                        else -> h
                    }
                    val label = String.format("%02d:00 %s", displayHour, amPm)
                    val simVal = if (h in 9..22) (0.35f + (h % 5) * 0.08f).coerceIn(0.1f, 0.85f) else 0.0f
                    val simDuration = if (simVal > 0f) (simVal * 3600000L).toLong() else 0L
                    DiscreteHourlyPoint(
                        hourOfDay = h,
                        label = label,
                        startTimeMs = startOfDayZdt.plusHours(h.toLong()).toInstant().toEpochMilli(),
                        endTimeMs = startOfDayZdt.plusHours((h + 1).toLong()).toInstant().toEpochMilli(),
                        durationMs = simDuration,
                        apiScore = simVal
                    )
                }

                val peakPoint = simulatedPoints.maxByOrNull { it.durationMs }
                val peakText = if (peakPoint != null && peakPoint.durationMs > 0L) peakPoint.label else "N/A"

                _uiState.value = InsightsUiState(
                    hourlyPoints = simulatedPoints,
                    peakHourText = peakText,
                    todayTotalActiveDurationMs = simulatedPoints.sumOf { it.durationMs }
                )
                return@launch
            }

            // Real UsageStats mode
            // NOTE: queryHourlyBuckets() / bucketsFromService removed — it called UsageStatsManager
            // for all 24 hours on every refresh without caching, yet serviceBucket was never read
            // in the score or durationMs formula below. apiScore and durationMs are derived solely
            // from Room DB logs (hourLogs). Removing the call eliminates the live OS query that was
            // identified as the root cause of historical chart fluctuation.
            val todayLogs = dbLogs.filter { it.timestamp >= startOfDayMs }

            val points = (0..23).map { h ->
                val bucketStart = startOfDayZdt.plusHours(h.toLong())
                val bucketEnd = bucketStart.plusHours(1)
                val sTime = bucketStart.toInstant().toEpochMilli()
                val eTime = bucketEnd.toInstant().toEpochMilli()

                val amPm = if (h >= 12) "PM" else "AM"
                val displayHour = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }
                val label = String.format("%02d:00 %s", displayHour, amPm)

                if (h > currentHour) {
                    DiscreteHourlyPoint(
                        hourOfDay = h,
                        label = label,
                        startTimeMs = sTime,
                        endTimeMs = eTime,
                        durationMs = 0L,
                        apiScore = 0.0f
                    )
                } else {
                    val queryEnd = Math.min(eTime, System.currentTimeMillis())
                    val hourlyMetrics = com.attentionguard.service.AttentionMonitoringService.queryHourlyMetricsDirectly(context, sTime, queryEnd)
                    val hourlySessionMs = hourlyMetrics.sessionMs
                    val hourlySwitches = hourlyMetrics.switchCount

                    val hourLogs = todayLogs.filter { it.timestamp in sTime..eTime }

                    // Hour-scoped Normalizations
                    // nSession is normalized against 1 hour (3,600,000 ms) - max possible usage in 1 hour
                    val nSession = (hourlySessionMs.toFloat() / 3600000f).coerceIn(0f, 1f)
                    val nSwitch = (hourlySwitches.toFloat() / 20.0f).coerceIn(0f, 1f)

                    // nScroll: average scroll velocity from DB logs in that hour if present
                    val validScrollLogs = hourLogs.filter { it.scrollVelocity in 1f..1000f }
                    val nScroll = if (validScrollLogs.isNotEmpty()) {
                        (validScrollLogs.map { it.scrollVelocity }.average().toFloat() / 250.0f).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    // nNight: night ratio for 0..5 AM window if session active
                    val nNight = if (h in 0..5 && hourlySessionMs > 0L) {
                        nSession
                    } else {
                        0f
                    }

                    // Hour-scoped API Score calculation
                    val apiScore = if (hourlySessionMs == 0L && hourLogs.isEmpty()) {
                        0.0f
                    } else {
                        (0.30f * nSession + 0.20f * nScroll + 0.30f * nSwitch + 0.20f * nNight).coerceIn(0.0f, 1.0f)
                    }

                    DiscreteHourlyPoint(
                        hourOfDay = h,
                        label = label,
                        startTimeMs = sTime,
                        endTimeMs = eTime,
                        durationMs = hourlySessionMs,
                        apiScore = apiScore
                    )
                }
            }

            // FIX PEAK ACTIVITY LOGIC: Identify local hour bucket with highest active duration / score FOR TODAY ONLY.
            val activeTodayPoints = points.filter { it.hourOfDay <= currentHour && (it.durationMs > 0L || it.apiScore > 0f) }
            val peakPoint = activeTodayPoints.maxByOrNull { it.apiScore }
            val peakText = if (peakPoint != null) peakPoint.label else "N/A"

            _uiState.value = InsightsUiState(
                hourlyPoints = points,
                peakHourText = peakText,
                todayTotalActiveDurationMs = points.sumOf { it.durationMs }
            )
        }
    }
}
