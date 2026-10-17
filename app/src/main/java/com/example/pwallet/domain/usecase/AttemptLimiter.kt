package com.example.pwallet.domain.usecase


import com.example.pwallet.domain.repository.StoredCredentials
import com.example.pwallet.domain.repository.UserRepository

class AttemptLimiter(
    private val users: UserRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    fun remainingLockMillis(credentials: StoredCredentials): Long =
        (credentials.lockedUntil - now()).coerceAtLeast(0)

    suspend fun onSuccess(credentials: StoredCredentials) =
        users.updateLoginState(credentials.user.id, failedAttempts = 0, lockedUntil = 0)


    suspend fun onFailure(credentials: StoredCredentials): Int? {
        val attempts = credentials.failedAttempts + 1
        return if (attempts >= MAX_ATTEMPTS) {
            users.updateLoginState(credentials.user.id, failedAttempts = 0, lockedUntil = now() + LOCK_MILLIS)
            null
        } else {
            users.updateLoginState(credentials.user.id, failedAttempts = attempts, lockedUntil = 0)
            MAX_ATTEMPTS - attempts
        }
    }

    companion object {
        const val MAX_ATTEMPTS = 5
        const val LOCK_MILLIS = 5 * 60 * 1000L
    }
}