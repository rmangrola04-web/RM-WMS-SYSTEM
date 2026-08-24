package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskName: String = "",
    val unitsCount: Int = 0,
    val status: String = "Compliant", // "Compliant", "Pending", "Non-Compliant"
    val dayLabel: String = "Today", // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today"
    val timestamp: Long = System.currentTimeMillis(),
    val remarks: String = "",
    val auditorName: String = "",
    val category: String = "Pest Control & Safety",
    val subActivity: String = "",
    val locationId: String = "",
    val refNumber: String = "",
    val quantity: String = "",
    val hubLocation: String = "ICH Hub - Indore",
    val duckLocation: String = ""
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val displayTitle: String
        get() = when {
            subActivity.isNotBlank() && category.isNotBlank() -> "$category - $subActivity"
            taskName.isNotBlank() -> taskName
            category.isNotBlank() -> category
            else -> "Godown Activity"
        }
}

