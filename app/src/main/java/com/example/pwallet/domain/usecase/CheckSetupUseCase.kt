package com.example.pwallet.domain.usecase

import com.example.pwallet.domain.repository.UserRepository


class CheckSetupUseCase(private val users: UserRepository) {
    suspend operator fun invoke(): Boolean = users.hasUsers()
}