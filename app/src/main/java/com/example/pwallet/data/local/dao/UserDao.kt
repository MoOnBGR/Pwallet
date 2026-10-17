package com.example.pwallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pwallet.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun findOnly(): UserEntity?

    @Query("UPDATE users SET failedAttempts = :attempts, lockedUntil = :lockedUntil WHERE id = :id")
    suspend fun updateLoginState(id: String, attempts: Int, lockedUntil: Long)

    @Query(
        """UPDATE users SET pinHash = :hash, pinSalt = :salt, pinIterations = :iterations,
           failedAttempts = 0, lockedUntil = 0, updatedAt = :now WHERE id = :id"""
    )
    suspend fun resetPin(id: String, hash: String, salt: String, iterations: Int, now: Long)
}
