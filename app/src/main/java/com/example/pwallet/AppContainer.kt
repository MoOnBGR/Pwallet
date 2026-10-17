package com.example.pwallet

import android.content.Context
import com.example.pwallet.data.local.AppDatabase
import com.example.pwallet.data.repository.AuditLoggerImpl
import com.example.pwallet.data.repository.ClientRepositoryImpl
import com.example.pwallet.data.repository.UserRepositoryImpl
import com.example.pwallet.domain.model.Session
import com.example.pwallet.domain.repository.AuditLogger
import com.example.pwallet.domain.repository.ClientRepository
import com.example.pwallet.domain.repository.UserRepository
import com.example.pwallet.domain.security.PasswordHasher
import com.example.pwallet.domain.usecase.CheckSetupUseCase
import com.example.pwallet.domain.usecase.CreateClientWithLoanUseCase
import com.example.pwallet.domain.usecase.LoginUseCase
import com.example.pwallet.domain.usecase.RegisterUserUseCase
import com.example.pwallet.domain.usecase.AttemptLimiter
import com.example.pwallet.domain.usecase.RecoverPinUseCase

class AppContainer private constructor(context: Context) {
    private val database = AppDatabase.get(context)
    private val users: UserRepository = UserRepositoryImpl(database.userDao())
    private val clients: ClientRepository = ClientRepositoryImpl(database)
    private val audit: AuditLogger = AuditLoggerImpl(database.auditDao())
    private val hasher = PasswordHasher()

    private val limiter = AttemptLimiter(users)

    val login = LoginUseCase(users, hasher, audit, limiter)
    val recoverPin = RecoverPinUseCase(users, hasher, audit, limiter)

    val session = Session()

    val checkSetup = CheckSetupUseCase(users)
    val registerUser = RegisterUserUseCase(users, hasher, audit)
    val createClientWithLoan = CreateClientWithLoanUseCase(clients, audit)

    companion object {
        @Volatile private var instance: AppContainer? = null

        fun get(context: Context): AppContainer = instance ?: synchronized(this) {
            instance ?: AppContainer(context.applicationContext).also { instance = it }
        }
    }
}