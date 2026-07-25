package com.attentionguard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttentionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttentionLog)

    // Returns ALL logs for today (from midnight onward).
    // Previously used LIMIT 150 which at 1 log/min only covered ~2.5 hours, causing early-morning
    // buckets to fall outside the window and show as flat "Low" by mid-day.
    @Query("SELECT * FROM attention_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getAllLogsFlow(startOfDay: Long): Flow<List<AttentionLog>>

    @Query("SELECT * FROM attention_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLog(): AttentionLog?

    @Query("SELECT * FROM attention_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AttentionLog>

    @Query("DELETE FROM attention_logs WHERE timestamp >= :timestamp")
    suspend fun deleteLogsAfter(timestamp: Long)
}
