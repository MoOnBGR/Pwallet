package com.example.pwallet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pwallet.AppContainer
import com.example.pwallet.domain.model.Session
import com.example.pwallet.domain.usecase.CheckSetupUseCase
import com.example.pwallet.domain.usecase.LoginResult
import com.example.pwallet.domain.usecase.LoginUseCase
import com.example.pwallet.domain.usecase.RecoverPinUseCase
import com.example.pwallet.domain.usecase.RecoverResult
import com.example.pwallet.domain.usecase.RegisterInput
import com.example.pwallet.domain.usecase.RegisterResult
import com.example.pwallet.domain.usecase.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val checkSetup: CheckSetupUseCase,
    private val registerUser: RegisterUserUseCase,
    private val login: LoginUseCase,
    private val recoverPin: RecoverPinUseCase,
    private val session: Session,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val configured = checkSetup()
            _state.update { it.copy(mode = if (configured) AuthMode.LOGIN else AuthMode.REGISTER) }
        }
    }

    // ---------- Login ----------

    fun onLoginChange(form: LoginForm) =
        _state.update { it.copy(login = form, loginError = null, notice = null) }

    fun submitLogin() {
        val current = _state.value
        if (current.isBusy) return
        _state.update { it.copy(isBusy = true, loginError = null) }
        viewModelScope.launch {
            val result = login(current.login.pin)
            if (result is LoginResult.Success) session.user = result.user
            _state.update { s ->
                when (result) {
                    is LoginResult.Success -> s.copy(isBusy = false, sessionUser = result.user, login = LoginForm())
                    else -> s.copy(isBusy = false, loginError = result.message(), login = LoginForm())
                }
            }
        }
    }

    fun logout() {
        session.user = null
        _state.update { it.copy(sessionUser = null, login = LoginForm()) }
    }

    // ---------- Registro ----------

    fun onRegisterChange(form: RegisterInput) =
        _state.update { it.copy(register = form, registerErrors = emptyMap()) }

    fun submitRegister() {
        val current = _state.value
        if (current.isBusy) return
        _state.update { it.copy(isBusy = true, registerErrors = emptyMap()) }
        viewModelScope.launch {
            val result = registerUser(current.register)
            if (result is RegisterResult.Success) session.user = result.user
            _state.update { s ->
                when (result) {
                    is RegisterResult.Success -> s.copy(
                        isBusy = false, sessionUser = result.user,
                        mode = AuthMode.LOGIN, register = RegisterInput(),
                    )
                    is RegisterResult.Invalid -> s.copy(isBusy = false, registerErrors = result.errors)
                    RegisterResult.AlreadyConfigured -> s.copy(isBusy = false, mode = AuthMode.LOGIN)
                }
            }
        }
    }

    // ---------- Recuperar PIN ----------

    fun startRecovery() {
        viewModelScope.launch {
            val question = recoverPin.question() ?: return@launch
            _state.update {
                it.copy(mode = AuthMode.RECOVER, recover = RecoverState(question = question), loginError = null)
            }
        }
    }

    fun cancelRecovery() = _state.update { it.copy(mode = AuthMode.LOGIN, recover = RecoverState()) }

    fun onRecoverChange(recover: RecoverState) = _state.update { it.copy(recover = recover.copy(error = null)) }

    fun verifyAnswer() {
        val current = _state.value
        if (current.isBusy) return
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val result = recoverPin.verifyAnswer(current.recover.answer)
            _state.update { s ->
                if (result == RecoverResult.Verified) {
                    s.copy(isBusy = false, recover = s.recover.copy(verified = true, error = null))
                } else {
                    s.copy(isBusy = false, recover = s.recover.copy(error = result.message()))
                }
            }
        }
    }

    fun submitNewPin() {
        val current = _state.value
        if (current.isBusy) return
        val recover = current.recover
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val result = recoverPin.resetPin(recover.answer, recover.newPin, recover.confirmPin)
            _state.update { s ->
                when (result) {
                    RecoverResult.PinChanged -> s.copy(
                        isBusy = false, mode = AuthMode.LOGIN, recover = RecoverState(),
                        login = LoginForm(), loginError = null,
                        notice = "PIN actualizado. Ingresa con tu nuevo PIN.",
                    )
                    is RecoverResult.InvalidPin ->
                        s.copy(isBusy = false, recover = s.recover.copy(error = result.message))
                    else -> // la respuesta ya no es válida (por ejemplo, por bloqueo): vuelve al paso 1
                        s.copy(isBusy = false, recover = s.recover.copy(verified = false, error = result.message()))
                }
            }
        }
    }

    // ---------- Mensajes ----------

    private fun lockMessage(remainingMillis: Long): String {
        val minutes = (remainingMillis + 59_999) / 60_000
        return "Demasiados intentos. Intenta de nuevo en $minutes min"
    }

    private fun LoginResult.message(): String = when (this) {
        is LoginResult.Success -> ""
        LoginResult.MissingFields -> "Ingresa tu PIN"
        LoginResult.NotConfigured -> "No hay un usuario configurado"
        is LoginResult.InvalidPin -> "PIN incorrecto. Intentos restantes: $attemptsLeft"
        is LoginResult.Locked -> lockMessage(remainingMillis)
    }

    private fun RecoverResult.message(): String = when (this) {
        RecoverResult.Verified, RecoverResult.PinChanged -> ""
        RecoverResult.MissingAnswer -> "Escribe tu respuesta"
        RecoverResult.NotConfigured -> "No hay un usuario configurado"
        is RecoverResult.WrongAnswer -> "Respuesta incorrecta. Intentos restantes: $attemptsLeft"
        is RecoverResult.Locked -> lockMessage(remainingMillis)
        is RecoverResult.InvalidPin -> message
    }
}

class AuthViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuthViewModel(
            container.checkSetup, container.registerUser, container.login,
            container.recoverPin, container.session,
        ) as T
}