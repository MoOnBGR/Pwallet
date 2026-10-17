package com.example.pwallet.domain.repository

import com.example.pwallet.domain.model.AuditAction
import com.example.pwallet.domain.model.Client
import com.example.pwallet.domain.model.Installment
import com.example.pwallet.domain.model.Loan
import com.example.pwallet.domain.model.User
import com.example.pwallet.domain.security.HashedPassword

data class StoredCredentials(
    val user: User,
    val pin: HashedPassword,
    val securityQuestion: String,
    val answer: HashedPassword,
    val failedAttempts: Int,
    val lockedUntil: Long,
)

interface UserRepository {
    suspend fun hasUsers(): Boolean
    suspend fun create(user: User, pin: HashedPassword, securityQuestion: String, answer: HashedPassword, now: Long)

    suspend fun findCredentials(): StoredCredentials?
    suspend fun updateLoginState(userId: String, failedAttempts: Int, lockedUntil: Long)
    suspend fun resetPin(userId: String, pin: HashedPassword, now: Long)
}

interface ClientRepository {

    suspend fun createClientWithLoan(client: Client, loan: Loan, installments: List<Installment>)
}

interface AuditLogger {
    suspend fun log(action: AuditAction, userId: String?, details: String? = null)
}