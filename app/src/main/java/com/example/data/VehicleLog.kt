package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Entity representing a vehicle movement log.
 * Stores vehicle plate number, log timestamp, and assigned dock information.
 */
@Entity(tableName = "vehicle_logs")
data class VehicleLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "plate_number")
    val plateNumber: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "assigned_dock")
    val assignedDock: String = "None",

    @ColumnInfo(name = "activity_type")
    val activityType: String = "Loading", // Loading, Unloading, Idle/Parking

    @ColumnInfo(name = "status")
    val status: String = "Gate In", // Gate In, Dock Assigned, In-Progress, Completed, Gate Out

    @ColumnInfo(name = "driver_name")
    val driverName: String = "",

    @ColumnInfo(name = "notes")
    val notes: String = ""
) {
    /**
     * Formats the timestamp epoch into a human-readable date & time string.
     */
    fun formattedTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
