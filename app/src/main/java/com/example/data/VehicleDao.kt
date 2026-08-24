package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle_entries ORDER BY id DESC")
    fun getAllEntries(): Flow<List<VehicleEntry>>

    @Query("SELECT * FROM vehicle_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): VehicleEntry?

    @Query("SELECT * FROM vehicle_entries WHERE status != 'Gate Out' ORDER BY id DESC")
    fun getActiveYardEntries(): Flow<List<VehicleEntry>>

    @Query("SELECT * FROM vehicle_entries WHERE status = 'Dock Assigned / Placed' OR status = 'In-Progress' ORDER BY id DESC")
    fun getDockedEntries(): Flow<List<VehicleEntry>>

    @Query("SELECT * FROM vehicle_entries WHERE dockBay = :bayName AND status != 'Gate Out' LIMIT 1")
    suspend fun getVehicleInBay(bayName: String): VehicleEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: VehicleEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<VehicleEntry>)

    @Update
    suspend fun updateEntry(entry: VehicleEntry)

    @Delete
    suspend fun deleteEntry(entry: VehicleEntry)

    @Query("DELETE FROM vehicle_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM vehicle_entries")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vehicle_entries WHERE status != 'Gate Out'")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vehicle_entries WHERE status = 'Dock Assigned / Placed' OR status = 'In-Progress'")
    fun getDockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vehicle_entries WHERE status = 'Completed' OR status = 'Gate Out'")
    fun getCompletedCount(): Flow<Int>
}
