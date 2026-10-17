package com.example.pwallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = Mint, onPrimary = Ink,
    secondary = Teal, onSecondary = Cream,
    background = Ink, onBackground = Cream,
    surface = Forest, onSurface = Cream,
    surfaceVariant = Forest, onSurfaceVariant = Mint,
    outline = Teal, error = ErrorSoft,
)

private val CardScheme = lightColorScheme(
    primary = Forest, onPrimary = Cream,
    secondary = Mint, onSecondary = Ink,
    background = Cream, onBackground = Ink,
    surface = Cream, onSurface = Ink,
    surfaceVariant = Cream, onSurfaceVariant = Teal,
    outline = Teal, error = ErrorStrong,
)

@Composable
fun PWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}


@Composable
fun PWalletCardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CardScheme, content = content)
}