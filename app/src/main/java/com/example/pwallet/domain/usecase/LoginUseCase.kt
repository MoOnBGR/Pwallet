package com.example.pwallet.domain.usecase

import com.example.pwallet.domain.model.AuditAction
import com.example.pwallet.domain.model.User
import com.example.pwallet.domain.repository.AuditLogger
import com.example.pwallet.domain.repository.UserRepository
import com.example.pwallet.domain.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface LoginResult {
    data class Success(val user: User) : LoginResult
    data object MissingFields : LoginResult
    data object NotConfigured : LoginResult
    data class InvalidPin(val attemptsLeft: Int) : LoginResult
    data class Locked(val remainingMillis: Long) : LoginResult
}

class LoginUseCase(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val audit: AuditLogger,
    private val limiter: AttemptLimiter,
) {
    suspend operator fun invoke(pin: String): LoginResult {
        if (pin.isEmpty()) return LoginResult.MissingFields
        val stored = users.findCredentials() ?: return LoginResult.NotConfigured

        val locked = limiter.remainingLockMillis(stored)
        if (locked > 0) return LoginResult.Locked(locked)

        val matches = withContext(Dispatchers.Default) { hasher.verify(pin, stored.pin) }
        if (matches) {
            limiter.onSuccess(stored)
            audit.log(AuditAction.LOGIN_SUCCESS, stored.user.id)
            return LoginResult.Success(stored.user)
        }

        audit.log(AuditAction.LOGIN_FAILED, stored.user.id)
        val attemptsLeft = limiter.onFailure(stored)
        return if (attemptsLeft == null) LoginResult.Locked(AttemptLimiter.LOCK_MILLIS)
        else LoginResult.InvalidPin(attemptsLeft)
    }
}