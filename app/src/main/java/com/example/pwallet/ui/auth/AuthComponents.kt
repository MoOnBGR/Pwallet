package com.example.pwallet.ui.auth


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pwallet.domain.model.Currency
import com.example.pwallet.ui.theme.Cream
import com.example.pwallet.ui.theme.Forest
import com.example.pwallet.ui.theme.Ink
import com.example.pwallet.ui.theme.Mint
import com.example.pwallet.ui.theme.PWalletCardTheme
import com.example.pwallet.ui.theme.Teal


@Composable
fun AuthHeader(title: String, subtitle: String, height: Dp) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .background(Ink)
            .clipToBounds()
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Forest, radius = size.width * 0.55f, center = Offset(size.width * 0.12f, size.height * 0.05f))
            drawCircle(Teal, radius = size.width * 0.26f, center = Offset(size.width * 0.93f, size.height * 0.28f))
            drawCircle(Mint, radius = size.width * 0.11f, center = Offset(size.width * 0.74f, size.height * 0.62f))
            drawCircle(Cream, radius = size.width * 0.04f, center = Offset(size.width * 0.50f, size.height * 0.30f))
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text("PWALLET", color = Mint, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text(title, color = Cream, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Cream.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AuthCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    PWalletCardTheme {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content,
            )
        }
    }
}

@Composable
fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isPassword: Boolean = false,
    forceLowercase: Boolean = false,
    onDone: () -> Unit = {},
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(if (forceLowercase) it.lowercase() else it) },
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            { TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") } }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword && keyboardType == KeyboardType.Text) KeyboardType.Password else keyboardType,
            imeAction = imeAction,
            capitalization = capitalization,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun PrimaryButton(text: String, busy: Boolean, onClick: () -> Unit) {
    Button(
        onClick = { if (!busy) onClick() },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun FormError(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
}


@Composable
fun CurrencySelector(selected: Currency?, error: String?, onSelect: (Currency) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Moneda", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Currency.entries.forEach { currency ->
                val isSelected = currency == selected
                val shape = RoundedCornerShape(16.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = shape,
                        )
                        .clickable { onSelect(currency) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(currency.symbol, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(currency.label, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (error != null) FormError(error)
    }
}