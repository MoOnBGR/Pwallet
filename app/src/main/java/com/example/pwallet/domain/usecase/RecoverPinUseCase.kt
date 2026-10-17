package com.example.pwallet.domain.usecase

import com.example.pwallet.domain.model.AuditAction
import com.example.pwallet.domain.repository.AuditLogger
import com.example.pwallet.domain.repository.UserRepository
import com.example.pwallet.domain.security.PasswordHasher
import com.example.pwallet.domain.security.PinPolicy
import com.example.pwallet.domain.security.normalizedAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface RecoverResult {
    data object Verified : RecoverResult
    data object PinChanged : RecoverResult
    data object MissingAnswer : RecoverResult
    data object NotConfigured : RecoverResult
    data class WrongAnswer(val attemptsLeft: Int) : RecoverResult
    data class Locked(val remainingMillis: Long) : RecoverResult
    data class InvalidPin(val message: String) : RecoverResult
}

class RecoverPinUseCase(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val audit: AuditLogger,
    private val limiter: AttemptLimiter,
) {
    suspend fun question(): String? = users.findCredentials()?.securityQuestion

    suspend fun verifyAnswer(answer: String): RecoverResult {
        val stored = users.findCredentials() ?: return RecoverResult.NotConfigured

        val locked = limiter.remainingLockMillis(stored)
        if (locked > 0) return RecoverResult.Locked(locked)

        val normalized = answer.normalizedAnswer()
        if (normalized.isEmpty()) return RecoverResult.MissingAnswer

        val matches = withContext(Dispatchers.Default) { hasher.verify(normalized, stored.answer) }
        if (matches) {
            limiter.onSuccess(stored)
            return RecoverResult.Verified
        }

        audit.log(AuditAction.RECOVERY_FAILED, stored.user.id)
        val attemptsLeft = limiter.onFailure(stored)
        return if (attemptsLeft == null) RecoverResult.Locked(AttemptLimiter.LOCK_MILLIS)
        else RecoverResult.WrongAnswer(attemptsLeft)
    }

    suspend fun resetPin(answer: String, newPin: String, confirmPin: String, now: Long = System.currentTimeMillis()): RecoverResult {
        val stored = users.findCredentials() ?: return RecoverResult.NotConfigured

        val locked = limiter.remainingLockMillis(stored)
        if (locked > 0) return RecoverResult.Locked(locked)

        val normalized = answer.normalizedAnswer()
        val matches = withContext(Dispatchers.Default) { hasher.verify(normalized, stored.answer) }
        if (!matches) {
            return verifyAnswer(answer)
        }

        val pinError = PinPolicy.formatError(newPin) ?: PinPolicy.mismatchError(newPin, confirmPin)
        if (pinError != null) {
            return RecoverResult.InvalidPin(pinError)
        }

        val newPinHash = withContext(Dispatchers.Default) { hasher.hash(newPin) }
        users.resetPin(stored.user.id, newPinHash, now)
        limiter.onSuccess(stored)
        audit.log(AuditAction.PIN_RESET, stored.user.id)
        return RecoverResult.PinChanged
    }
}
