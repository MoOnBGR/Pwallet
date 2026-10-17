package com.example.pwallet.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pwallet.domain.model.Currency
import com.example.pwallet.domain.model.PaymentFrequency
import com.example.pwallet.domain.model.formatMoney
import com.example.pwallet.domain.usecase.ClientField
import com.example.pwallet.domain.usecase.ClientLoanForm
import com.example.pwallet.domain.usecase.LoanPlan
import com.example.pwallet.ui.auth.AuthCard
import com.example.pwallet.ui.auth.AuthField
import com.example.pwallet.ui.auth.FormError
import com.example.pwallet.ui.auth.PrimaryButton
import com.example.pwallet.ui.common.DateField
import com.example.pwallet.ui.common.OptionRow
import com.example.pwallet.ui.common.label
import com.example.pwallet.ui.common.toDisplay
import com.example.pwallet.ui.theme.Cream
import com.example.pwallet.ui.theme.Forest
import com.example.pwallet.ui.theme.Mint

@Composable
fun CreateClientScreen(
    state: CreateClientUiState,
    currency: Currency,
    onChange: (ClientLoanForm) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val form = state.form
    val errors = state.errors

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                "Nuevo cliente",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        AuthCard(Modifier.weight(1f)) {
            SectionTitle("Datos del cliente")
            AuthField(
                label = "Nombre",
                value = form.firstName,
                onValueChange = { onChange(form.copy(firstName = it)) },
                error = errors[ClientField.FIRST_NAME],
                capitalization = KeyboardCapitalization.Words,
            )
            AuthField(
                label = "Primer apellido",
                value = form.lastName1,
                onValueChange = { onChange(form.copy(lastName1 = it)) },
                error = errors[ClientField.LAST_NAME_1],
                capitalization = KeyboardCapitalization.Words,
            )
            AuthField(
                label = "Segundo apellido",
                value = form.lastName2,
                onValueChange = { onChange(form.copy(lastName2 = it)) },
                error = errors[ClientField.LAST_NAME_2],
                capitalization = KeyboardCapitalization.Words,
            )
            AuthField(
                label = "Celular",
                value = form.phone,
                onValueChange = { onChange(form.copy(phone = it)) },
                error = errors[ClientField.PHONE],
                keyboardType = KeyboardType.Phone,
            )
            AuthField(
                label = "Cédula (opcional)",
                value = form.nationalId,
                onValueChange = { onChange(form.copy(nationalId = it)) },
            )
            AuthField(
                label = "Correo (opcional)",
                value = form.email,
                onValueChange = { onChange(form.copy(email = it)) },
                error = errors[ClientField.EMAIL],
                keyboardType = KeyboardType.Email,
            )

            SectionTitle("Detalle del préstamo")
            DateField(
                label = "Fecha del préstamo",
                value = form.loanDate,
            ) { onChange(form.copy(loanDate = it)) }
            AuthField(
                label = "Monto prestado, neto (${currency.symbol})",
                value = form.principal,
                onValueChange = { onChange(form.copy(principal = it)) },
                error = errors[ClientField.PRINCIPAL],
                keyboardType = KeyboardType.Decimal,
            )
            AuthField(
                label = "Interés (%)",
                value = form.interestPercent,
                onValueChange = { onChange(form.copy(interestPercent = it)) },
                error = errors[ClientField.INTEREST],
                keyboardType = KeyboardType.Decimal,
            )
            AuthField(
                label = "Número de cuotas",
                value = form.installments,
                onValueChange = { onChange(form.copy(installments = it)) },
                error = errors[ClientField.INSTALLMENTS],
                keyboardType = KeyboardType.Number,
            )
            OptionRow(
                title = "Frecuencia de pago",
                options = PaymentFrequency.entries,
                selected = form.frequency,
                label = { it.label() },
                onSelect = { onChange(form.copy(frequency = it)) },
            )
            DateField(
                label = "Fecha del primer pago",
                value = form.firstDueDate,
                placeholder = "Automática (según frecuencia)",
                error = errors[ClientField.FIRST_DUE_DATE],
            ) { onChange(form.copy(firstDueDate = it)) }
            if (form.firstDueDate != null) {
                TextButton(onClick = { onChange(form.copy(firstDueDate = null)) }) {
                    Text("Volver a fecha automática")
                }
            }
            AuthField(
                label = "Garantía (opcional)",
                value = form.collateral,
                onValueChange = { onChange(form.copy(collateral = it)) },
                imeAction = ImeAction.Done,
            )

            LoanSummary(state.preview, currency)

            state.saveError?.let { FormError(it) }
            PrimaryButton("Guardar cliente", state.isSaving, onSave)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun LoanSummary(plan: LoanPlan?, currency: Currency) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Forest)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (plan == null) {
            Text(
                "Completa monto, interés y cuotas para ver el resumen.",
                color = Cream.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            val first = plan.installments.first()
            SummaryRow("Neto a pagar (capital)", formatMoney(plan.principal, currency))
            SummaryRow("Interés", formatMoney(plan.interest, currency))
            SummaryRow("Total con interés", formatMoney(plan.total, currency), highlight = true)
            SummaryRow("Cuotas", "${plan.installments.size} × ${formatMoney(first.amount, currency)}")
            SummaryRow("Primer pago", first.dueDate.toDisplay())
            SummaryRow("Último pago", plan.installments.last().dueDate.toDisplay())
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Cream.copy(alpha = 0.8f))
        Text(
            value,
            color = if (highlight) Mint else Cream,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
        )
    }
}