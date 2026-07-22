package com.attentionguard.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attention_logs")
data class AttentionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "session_duration") val sessionDuration: Float,
    @ColumnInfo(name = "scroll_velocity") val scrollVelocity: Float,
    @ColumnInfo(name = "task_switches") val taskSwitches: Float,
    @ColumnInfo(name = "night_ratio") val nightRatio: Float,
    @ColumnInfo(name = "api_score") val apiScore: Float,
    @ColumnInfo(name = "risk_tier") val riskTier: String,
    @ColumnInfo(name = "is_alert_event", defaultValue = "0") val isAlertEvent: Boolean = false
)
