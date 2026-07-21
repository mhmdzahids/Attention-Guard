package com.attentionguard.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttentionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttentionLog)

    @Query("SELECT * FROM attention_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<AttentionLog>>

    @Query("SELECT * FROM attention_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLog(): AttentionLog?

    @Query("SELECT * FROM attention_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AttentionLog>

    @Query("DELETE FROM attention_logs WHERE timestamp >= :timestamp")
    suspend fun deleteLogsAfter(timestamp: Long)
}
