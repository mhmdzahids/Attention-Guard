package com.attentionguard.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attentionguard.data.AttentionLog
import com.attentionguard.service.AttentionMonitoringService
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
            val bucketsFromService = AttentionMonitoringService.queryHourlyBuckets(context)
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

                val serviceBucket = bucketsFromService.find { it.hour == h }
                val hourLogs = todayLogs.filter { it.timestamp in sTime..eTime }

                // STRICT DB-LOG & ZERO-FILL CHECK:
                // An hour bucket receives a non-zero API score ONLY if Room DB contains recorded logs for that specific hour interval.
                val apiScore = when {
                    h > currentHour -> 0.0f
                    hourLogs.isNotEmpty() -> hourLogs.map { it.apiScore }.average().toFloat().coerceIn(0.0f, 1.0f)
                    else -> 0.0f // STRICT 0.0f FOR MISSING / EMPTY BUCKETS (No fallback baselines or service defaults)
                }

                val durationMs = if (hourLogs.isNotEmpty()) {
                    (hourLogs.map { it.sessionDuration }.average() * 3600000L).toLong()
                } else {
                    0L
                }

                DiscreteHourlyPoint(
                    hourOfDay = h,
                    label = label,
                    startTimeMs = sTime,
                    endTimeMs = eTime,
                    durationMs = durationMs,
                    apiScore = apiScore
                )
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
