package com.example.pwallet.data.repository

import com.example.pwallet.data.local.dao.UserDao
import com.example.pwallet.data.local.entity.UserEntity
import com.example.pwallet.domain.model.User
import com.example.pwallet.domain.repository.StoredCredentials
import com.example.pwallet.domain.repository.UserRepository
import com.example.pwallet.domain.security.HashedPassword

class UserRepositoryImpl(private val dao: UserDao) : UserRepository {

    override suspend fun hasUsers() = dao.count() > 0

    override suspend fun create(
        user: User,
        pin: HashedPassword,
        securityQuestion: String,
        answer: HashedPassword,
        now: Long,
    ) {
        dao.insert(
            UserEntity(
                id = user.id,
                fullName = user.fullName,
                phone = user.phone,
                currency = user.currency,
                businessName = user.businessName,
                pinHash = pin.hash,
                pinSalt = pin.salt,
                pinIterations = pin.iterations,
                securityQuestion = securityQuestion,
                answerHash = answer.hash,
                answerSalt = answer.salt,
                answerIterations = answer.iterations,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun findCredentials(): StoredCredentials? = dao.findOnly()?.let { e ->
        StoredCredentials(
            user = User(e.id, e.fullName, e.phone, e.currency, e.businessName),
            pin = HashedPassword(e.pinHash, e.pinSalt, e.pinIterations),
            securityQuestion = e.securityQuestion,
            answer = HashedPassword(e.answerHash, e.answerSalt, e.answerIterations),
            failedAttempts = e.failedAttempts,
            lockedUntil = e.lockedUntil,
        )
    }

    override suspend fun updateLoginState(userId: String, failedAttempts: Int, lockedUntil: Long) =
        dao.updateLoginState(userId, failedAttempts, lockedUntil)

    override suspend fun resetPin(userId: String, pin: HashedPassword, now: Long) =
        dao.resetPin(userId, pin.hash, pin.salt, pin.iterations, now)
}