package com.example.pwallet.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pwallet.domain.security.PinPolicy
import com.example.pwallet.domain.usecase.RegisterField
import com.example.pwallet.domain.usecase.RegisterInput

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onChange: (RegisterInput) -> Unit,
    onSubmit: () -> Unit,
) {
    val form = state.register
    val errors = state.registerErrors
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        AuthHeader("Crea tu usuario", "Configura tu billetera en este celular", 160.dp)
        AuthCard(Modifier.weight(1f)) {
            AuthField(
                label = "Nombre completo",
                value = form.fullName,
                onValueChange = { onChange(form.copy(fullName = it)) },
                error = errors[RegisterField.FULL_NAME],
                capitalization = KeyboardCapitalization.Words,
            )
            AuthField(
                label = "Celular",
                value = form.phone,
                onValueChange = { onChange(form.copy(phone = it)) },
                error = errors[RegisterField.PHONE],
                keyboardType = KeyboardType.Phone,
            )

            Text(
                "Tu PIN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            AuthField(
                label = "PIN de ${PinPolicy.LENGTH} dígitos",
                value = form.pin,
                onValueChange = { onChange(form.copy(pin = PinPolicy.keep(it))) },
                error = errors[RegisterField.PIN],
                isPassword = true,
                keyboardType = KeyboardType.NumberPassword,
            )
            AuthField(
                label = "Confirmar PIN",
                value = form.confirmPin,
                onValueChange = { onChange(form.copy(confirmPin = PinPolicy.keep(it))) },
                error = errors[RegisterField.CONFIRM_PIN],
                isPassword = true,
                keyboardType = KeyboardType.NumberPassword,
            )

            Text(
                "Pregunta de seguridad (para recuperar tu PIN)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            AuthField(
                label = "Tu pregunta",
                value = form.question,
                onValueChange = { onChange(form.copy(question = it)) },
                error = errors[RegisterField.QUESTION],
                forceLowercase = true,
            )
            AuthField(
                label = "Tu respuesta",
                value = form.answer,
                onValueChange = { onChange(form.copy(answer = it)) },
                error = errors[RegisterField.ANSWER],
                forceLowercase = true,
                imeAction = ImeAction.Done,
                onDone = onSubmit,
            )
            Text(
                "Elige una respuesta que solo tú sepas: con ella se puede restablecer tu PIN.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CurrencySelector(
                selected = form.currency,
                error = errors[RegisterField.CURRENCY],
                onSelect = { onChange(form.copy(currency = it)) },
            )

            PrimaryButton("Crear usuario", state.isBusy, onSubmit)
        }
    }
}