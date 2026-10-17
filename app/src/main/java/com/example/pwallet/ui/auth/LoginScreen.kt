package com.example.pwallet.ui.auth

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
fun LoginScreen(
    state: AuthUiState,
    onChange: (LoginForm) -> Unit,
    onSubmit: () -> Unit,
    onForgot: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        AuthHeader("Bienvenido de nuevo", "Ingresa tu PIN para continuar", 220.dp)
        AuthCard(Modifier.weight(1f)) {
            Text("Ingresa tu PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AuthField(
                label = "PIN",
                value = state.login.pin,
                onValueChange = { onChange(LoginForm(PinPolicy.keep(it))) },
                isPassword = true,
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
                onDone = onSubmit,
            )
            state.notice?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
            state.loginError?.let { FormError(it) }
            PrimaryButton("Entrar", state.isBusy, onSubmit)
            TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("¿Olvidaste tu PIN?")
            }
        }
    }
}