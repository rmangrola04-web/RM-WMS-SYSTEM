package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["mobile_number"], unique = false),
        Index(value = ["email"], unique = false)
    ]
)
data class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String = generateUserId(),

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "category", defaultValue = "Operator")
    val category: String = "Operator",

    @ColumnInfo(name = "mobile_number", defaultValue = "")
    val mobileNumber: String = "",

    @ColumnInfo(name = "email", defaultValue = "")
    val email: String = "",

    @ColumnInfo(name = "password_hash", defaultValue = "")
    val passwordHash: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: String = currentTimestamp(),

    @ColumnInfo(name = "last_login")
    val lastLogin: String? = null,

    @ColumnInfo(name = "status", defaultValue = "Active")
    val status: String = "Active"
) {
    companion object {
        val CATEGORIES = listOf(
            "Security Guard" to "Security Guard (सुरक्षा गार्ड)",
            "Supervisor" to "Supervisor (सुपरवाइजर)",
            "Operator" to "Operator (ऑपरेटर)",
            "Manager" to "Manager (मैनेजर)",
            "Other" to "Other (अन्य)"
        )

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun generateUserId(): String {
            val hex = UUID.randomUUID().toString().replace("-", "").take(6).uppercase(Locale.ROOT)
            return "USR-$hex"
        }

        fun currentTimestamp(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
