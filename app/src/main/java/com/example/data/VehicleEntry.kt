package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "vehicle_entries")
data class VehicleEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityType: String,       // Loading, Unloading, Cross-Docking
    val vehicleNumber: String,      // e.g. MP09 AB 1234
    val vehicleType: String,        // 14 Feet, 20 Feet, 24 Feet Single Axle, etc.
    val transporter: String = "",   // e.g. V-Trans, TCI
    val placedTime: String = "",    // "YYYY-MM-DD HH:mm" or ISO (Dock Reaching Time)
    val fromLocation: String,       // Origin / Source Location
    val toLocation: String,         // Destination Location
    val nextDestination: String = "", // Next Destination after Unloading
    val inTime: String = "",        // Gate In Time
    val outTime: String = "",       // Gate Out Time
    val status: String = "Gate In", // Gate In, Dock Assigned / Placed, In-Progress, Completed, Gate Out
    val remarks: String = "",       // Remarks / Damage status / Delay reason
    val dockBay: String = "",       // e.g. "Bay 01", "Bay 04"
    val driverName: String = "",
    val driverPhone: String = "",
    val cartonsCount: Int = 0,      // Number of Cartons (Loading)
    val sealNumber: String = "",    // Seal Number (Loading)
    val operationStartTime: String = "", // Loading/Unloading Start Time
    val operationEndTime: String = "",   // Loading/Unloading End Time
    val grnNumber: String = "",     // GRN Number (Unloading)
    val grnTime: String = "",       // GRN Time (Unloading)
    val lrNumber: String = "",      // LR / Bilty Number (Unloading)
    val invoiceFile: String = "",
    val lrFile: String = "",
    val checklistDone: Boolean = false,
    val hasDiscrepancy: Boolean = false,
    val discrepancyType: String = "",
    val discrepancyFile: String = "",
    val discrepancyRemarks: String = "",
    val operatorName: String = "",
    val operatorCategory: String = "",
    val operatorMobile: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object VehicleConstants {
    val HUB_LOCATIONS = listOf(
        "ICH Indore",
        "AIL ICH Indore",
        "HPL"
    )

    val ACTIVITY_TYPES = listOf(
        "Unloading" to "Unloading (आवक / खाली हो रही है)",
        "Loading" to "Loading (जावक / माल लोड हो रहा है)"
    )

    val VEHICLE_TYPES = listOf(
        "14 Feet",
        "20 Feet",
        "24 Feet Single Axle",
        "24 Feet Multi Axle",
        "32 Feet Single Axle",
        "32 Feet Multi Axle",
        "Container",
        "Tata Ace / Pick-up",
        "40 Feet Trailer",
        "Other"
    )

    val STATUS_OPTIONS = listOf(
        "Gate In" to "Gate In",
        "Dock Assigned / Placed" to "Dock Assigned / Placed",
        "In-Progress" to "In-Progress",
        "Completed" to "Completed / Ready",
        "Gate Out" to "Gate Out"
    )

    val DOCK_BAYS = listOf(
        "None",
        "Bay 01",
        "Bay 02",
        "Bay 03",
        "Bay 04",
        "Bay 05",
        "Bay 06",
        "Bay 07",
        "Bay 08",
        "Bay 09",
        "Bay 10"
    )

    val COMMON_LOCATIONS = listOf(
        "Indore Plant",
        "Pithampur Industrial Area",
        "Dewas DC",
        "Bhopal Warehouse",
        "Mumbai Central Hub",
        "Delhi NCR Logistics Park",
        "Ahmedabad Gateway",
        "Pune Distribution Center",
        "Nagpur Logistics Hub",
        "Surat Warehouse"
    )

    fun currentFormattedDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
