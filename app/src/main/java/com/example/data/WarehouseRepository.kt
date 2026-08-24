package com.example.data

import kotlinx.coroutines.flow.Flow

class WarehouseRepository(
    private val vehicleDao: VehicleDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao? = null
) {
    val allEntries: Flow<List<VehicleEntry>> = vehicleDao.getAllEntries()
    val activeEntries: Flow<List<VehicleEntry>> = vehicleDao.getActiveYardEntries()
    val dockedEntries: Flow<List<VehicleEntry>> = vehicleDao.getDockedEntries()
    
    val totalCount: Flow<Int> = vehicleDao.getTotalCount()
    val activeCount: Flow<Int> = vehicleDao.getActiveCount()
    val dockedCount: Flow<Int> = vehicleDao.getDockedCount()
    val completedCount: Flow<Int> = vehicleDao.getCompletedCount()

    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    val allActivityLogs: Flow<List<ActivityLog>> = activityLogDao?.getAllActivityLogs() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertActivityLog(log: ActivityLog): Long {
        return activityLogDao?.insertActivityLog(log) ?: -1L
    }

    suspend fun deleteActivityLog(log: ActivityLog) {
        activityLogDao?.deleteActivityLog(log)
    }

    // 2. Register New User
    suspend fun registerUser(fullName: String, email: String, password: String): Pair<Boolean, String> {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = fullName.trim()
        
        if (trimmedEmail.isBlank() || password.isBlank() || trimmedName.isBlank()) {
            return Pair(false, "All fields are required")
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Pair(false, "This email is already registered!")
        }

        val userId = User.generateUserId()
        val pwdHash = User.hashPassword(password)
        val createdAt = User.currentTimestamp()

        val newUser = User(
            userId = userId,
            fullName = trimmedName,
            email = trimmedEmail,
            passwordHash = pwdHash,
            createdAt = createdAt,
            status = "Active"
        )

        return try {
            userDao.insertUser(newUser)
            Pair(true, userId)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Registration failed")
        }
    }

    // 3. User Login Verification
    suspend fun loginUser(email: String, password: String): Pair<Boolean, User?> {
        val trimmedEmail = email.trim().lowercase()
        val pwdHash = User.hashPassword(password)
        val user = userDao.verifyLogin(trimmedEmail, pwdHash)
        
        return if (user != null) {
            val now = User.currentTimestamp()
            userDao.updateLastLogin(user.userId, now)
            val updatedUser = user.copy(lastLogin = now)
            Pair(true, updatedUser)
        } else {
            Pair(false, null)
        }
    }

    suspend fun loginOrRegisterOtpUser(fullName: String, category: String, mobileNumber: String): User {
        val trimmedMobile = mobileNumber.trim()
        val trimmedName = fullName.trim()
        val trimmedCategory = category.trim().ifBlank { "Operator" }
        val now = User.currentTimestamp()

        val existingUser = userDao.getUserByMobile(trimmedMobile)
        return if (existingUser != null) {
            val updated = existingUser.copy(
                fullName = if (trimmedName.isNotBlank()) trimmedName else existingUser.fullName,
                category = trimmedCategory,
                lastLogin = now
            )
            userDao.updateUser(updated)
            updated
        } else {
            val newUser = User(
                userId = User.generateUserId(),
                fullName = if (trimmedName.isNotBlank()) trimmedName else "Warehouse Operator",
                category = trimmedCategory,
                mobileNumber = trimmedMobile,
                email = "${trimmedMobile}@warehouse.internal",
                passwordHash = "",
                createdAt = now,
                lastLogin = now,
                status = "Active"
            )
            userDao.insertUser(newUser)
            newUser
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email.trim().lowercase())
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getEntryById(id: Long): VehicleEntry? {
        return vehicleDao.getEntryById(id)
    }

    suspend fun insertEntry(entry: VehicleEntry): Long {
        return vehicleDao.insertEntry(entry)
    }

    suspend fun updateEntry(entry: VehicleEntry) {
        vehicleDao.updateEntry(entry.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteEntry(entry: VehicleEntry) {
        vehicleDao.deleteEntry(entry)
    }

    suspend fun deleteById(id: Long) {
        vehicleDao.deleteById(id)
    }

    suspend fun updateStatus(id: Long, newStatus: String, timestamp: String) {
        val entry = vehicleDao.getEntryById(id) ?: return
        val updated = when (newStatus) {
            "Dock Assigned / Placed" -> entry.copy(
                status = newStatus,
                placedTime = if (entry.placedTime.isBlank()) timestamp else entry.placedTime
            )
            "In-Progress" -> entry.copy(
                status = newStatus,
                placedTime = if (entry.placedTime.isBlank()) timestamp else entry.placedTime
            )
            "Completed" -> entry.copy(
                status = newStatus
            )
            "Gate Out" -> entry.copy(
                status = newStatus,
                outTime = if (entry.outTime.isBlank()) timestamp else entry.outTime,
                dockBay = "None"
            )
            else -> entry.copy(status = newStatus)
        }
        vehicleDao.updateEntry(updated.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun assignDockBay(id: Long, bay: String) {
        val entry = vehicleDao.getEntryById(id) ?: return
        val updated = entry.copy(
            dockBay = bay,
            status = if (entry.status == "Gate In") "Dock Assigned / Placed" else entry.status,
            placedTime = if (entry.placedTime.isBlank() && bay != "None") VehicleConstants.currentFormattedDateTime() else entry.placedTime,
            updatedAt = System.currentTimeMillis()
        )
        vehicleDao.updateEntry(updated)
    }
}
