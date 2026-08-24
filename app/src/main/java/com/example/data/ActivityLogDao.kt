package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivityLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE status = :status ORDER BY timestamp DESC")
    fun getLogsByStatus(status: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ActivityLog>)

    @Update
    suspend fun updateActivityLog(log: ActivityLog)

    @Delete
    suspend fun deleteActivityLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllActivityLogs()

    @Query("SELECT COUNT(*) FROM activity_logs")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM activity_logs WHERE status = 'Compliant'")
    suspend fun getCompliantCount(): Int

    @Query("SELECT COUNT(*) FROM activity_logs WHERE status = 'Pending'")
    suspend fun getPendingCount(): Int

    @Query("SELECT COUNT(*) FROM activity_logs WHERE status = 'Non-Compliant'")
    suspend fun getNonCompliantCount(): Int
}
