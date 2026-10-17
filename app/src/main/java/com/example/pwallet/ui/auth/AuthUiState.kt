package com.example.pwallet.ui.auth

import com.example.pwallet.domain.model.User
import com.example.pwallet.domain.usecase.RegisterField
import com.example.pwallet.domain.usecase.RegisterInput

enum class AuthMode { LOADING, LOGIN, REGISTER, RECOVER }

data class LoginForm(val pin: String = "")


data class RecoverState(
    val question: String = "",
    val answer: String = "",
    val verified: Boolean = false,
    val newPin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
)

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOADING,
    val login: LoginForm = LoginForm(),
    val loginError: String? = null,
    val notice: String? = null,
    val register: RegisterInput = RegisterInput(),
    val registerErrors: Map<RegisterField, String> = emptyMap(),
    val recover: RecoverState = RecoverState(),
    val isBusy: Boolean = false,
    val sessionUser: User? = null,
)