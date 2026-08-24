package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for VehicleLog CRUD operations in Room.
 */
@Dao
interface VehicleLogDao {

    /**
     * Create: Insert a new vehicle log entry.
     * @return The auto-generated row ID of the inserted vehicle log.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VehicleLog): Long

    /**
     * Create: Insert multiple vehicle logs in batch.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(vararg logs: VehicleLog)

    /**
     * Read: Retrieve all vehicle logs ordered by timestamp descending (newest first).
     */
    @Query("SELECT * FROM vehicle_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<VehicleLog>>

    /**
     * Read: Retrieve a single vehicle log by its primary key ID.
     */
    @Query("SELECT * FROM vehicle_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): VehicleLog?

    /**
     * Read: Search logs for a specific license plate number.
     */
    @Query("SELECT * FROM vehicle_logs WHERE plate_number = :plateNumber ORDER BY timestamp DESC")
    fun getLogsByPlateNumber(plateNumber: String): Flow<List<VehicleLog>>

    /**
     * Read: Retrieve logs filtered by an assigned dock bay.
     */
    @Query("SELECT * FROM vehicle_logs WHERE assigned_dock = :assignedDock ORDER BY timestamp DESC")
    fun getLogsByDock(assignedDock: String): Flow<List<VehicleLog>>

    /**
     * Read: Retrieve the latest N vehicle logs.
     */
    @Query("SELECT * FROM vehicle_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<VehicleLog>>

    /**
     * Update: Modify an existing vehicle log.
     */
    @Update
    suspend fun updateLog(log: VehicleLog): Int

    /**
     * Delete: Remove a specific vehicle log from the database.
     */
    @Delete
    suspend fun deleteLog(log: VehicleLog): Int

    /**
     * Delete: Remove a vehicle log by its ID.
     */
    @Query("DELETE FROM vehicle_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long): Int

    /**
     * Delete: Remove all vehicle logs.
     */
    @Query("DELETE FROM vehicle_logs")
    suspend fun deleteAllLogs(): Int
}
