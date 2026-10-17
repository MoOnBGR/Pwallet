package com.example.pwallet.domain.usecase

import com.example.pwallet.domain.model.AuditAction
import com.example.pwallet.domain.model.Currency
import com.example.pwallet.domain.model.User
import com.example.pwallet.domain.repository.AuditLogger
import com.example.pwallet.domain.repository.UserRepository
import com.example.pwallet.domain.security.PasswordHasher
import com.example.pwallet.domain.security.PinPolicy
import com.example.pwallet.domain.security.normalizedAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class RegisterInput(
    val fullName: String = "",
    val phone: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val currency: Currency? = null,
    val question: String = "",
    val answer: String = "",
)

enum class RegisterField { FULL_NAME, PHONE, PIN, CONFIRM_PIN, CURRENCY, QUESTION, ANSWER }

sealed interface RegisterResult {
    data class Success(val user: User) : RegisterResult
    data class Invalid(val errors: Map<RegisterField, String>) : RegisterResult
    data object AlreadyConfigured : RegisterResult
}

class RegisterUserUseCase(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val audit: AuditLogger,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(input: RegisterInput): RegisterResult {
        if (users.hasUsers()) return RegisterResult.AlreadyConfigured

        val errors = validate(input)
        if (errors.isNotEmpty()) return RegisterResult.Invalid(errors)

        // PBKDF2 es lento a propósito: se ejecuta fuera del hilo de la pantalla.
        val (pinHash, answerHash) = withContext(Dispatchers.Default) {
            hasher.hash(input.pin) to hasher.hash(input.answer.normalizedAnswer())
        }

        val user = User(
            id = newId(),
            fullName = input.fullName.trim().replace(Regex("\\s+"), " "),
            phone = input.phone.filter(Char::isDigit),
            currency = requireNotNull(input.currency),
            businessName = BUSINESS_NAME,
        )
        val question = input.question.trim().replace(Regex("\\s+"), " ").lowercase()
        users.create(user, pinHash, question, answerHash, now())
        audit.log(AuditAction.USER_CREATED, user.id)
        return RegisterResult.Success(user)
    }

    private fun validate(i: RegisterInput): Map<RegisterField, String> = buildMap {
        if (i.fullName.trim().split(Regex("\\s+")).size < 2) {
            put(RegisterField.FULL_NAME, "Ingresa nombre y apellido")
        }
        if (i.phone.filter(Char::isDigit).length !in 8..15) {
            put(RegisterField.PHONE, "Número de celular inválido")
        }
        val pinError = PinPolicy.formatError(i.pin)
        if (pinError != null) {
            put(RegisterField.PIN, pinError)
        } else {
            PinPolicy.mismatchError(i.pin, i.confirmPin)?.let { put(RegisterField.CONFIRM_PIN, it) }
        }
        if (i.currency == null) put(RegisterField.CURRENCY, "Selecciona una moneda")
        if (i.question.trim().length < 8) put(RegisterField.QUESTION, "Escribe una pregunta (mínimo 8 caracteres)")
        if (i.answer.normalizedAnswer().length < 3) put(RegisterField.ANSWER, "Respuesta muy corta (mínimo 3 caracteres)")
    }

    private companion object {
        const val BUSINESS_NAME = "PWALLET"
    }
}