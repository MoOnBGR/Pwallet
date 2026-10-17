package com.example.pwallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pwallet.domain.model.Currency

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phone: String,
    val currency: Currency,
    val businessName: String,
    val pinHash: String,
    val pinSalt: String,
    val pinIterations: Int,
    val securityQuestion: String,
    val answerHash: String,
    val answerSalt: String,
    val answerIterations: Int,
    val failedAttempts: Int = 0,
    val lockedUntil: Long = 0,
    val createdAt: Long,
    val updatedAt: Long,
)
