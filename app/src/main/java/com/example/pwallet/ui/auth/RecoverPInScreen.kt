package com.example.pwallet.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pwallet.domain.security.PinPolicy
@Composable
fun RecoverPinScreen(
    state: AuthUiState,
    onChange: (RecoverState) -> Unit,
    onVerify: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val recover = state.recover
    BackHandler(onBack = onBack)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        AuthHeader(
            title = if (recover.verified) "Crea tu nuevo PIN" else "Recupera tu PIN",
            subtitle = if (recover.verified) "Elige un PIN de ${PinPolicy.LENGTH} dígitos"
            else "Responde tu pregunta de seguridad",
            height = 200.dp,
        )
        AuthCard(Modifier.weight(1f)) {
            if (!recover.verified) {
                Text("Tu pregunta", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(recover.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AuthField(
                    label = "Tu respuesta",
                    value = recover.answer,
                    onValueChange = { onChange(recover.copy(answer = it)) },
                    forceLowercase = true,
                    imeAction = ImeAction.Done,
                    onDone = onVerify,
                )
                recover.error?.let { FormError(it) }
                PrimaryButton("Verificar respuesta", state.isBusy, onVerify)
            } else {
                AuthField(
                    label = "Nuevo PIN",
                    value = recover.newPin,
                    onValueChange = { onChange(recover.copy(newPin = PinPolicy.keep(it))) },
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword,
                )
                AuthField(
                    label = "Confirmar nuevo PIN",
                    value = recover.confirmPin,
                    onValueChange = { onChange(recover.copy(confirmPin = PinPolicy.keep(it))) },
                    isPassword = true,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                    onDone = onSave,
                )
                recover.error?.let { FormError(it) }
                PrimaryButton("Guardar nuevo PIN", state.isBusy, onSave)
            }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}