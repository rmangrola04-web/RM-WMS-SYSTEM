package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE mobile_number = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password_hash = :passwordHash LIMIT 1")
    suspend fun verifyLogin(email: String, passwordHash: String): User?

    @Query("UPDATE users SET last_login = :timestamp, category = :category WHERE user_id = :userId")
    suspend fun updateLoginAndCategory(userId: String, category: String, timestamp: String)

    @Query("UPDATE users SET last_login = :timestamp WHERE user_id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: String)

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Update
    suspend fun updateUser(user: User)
}
