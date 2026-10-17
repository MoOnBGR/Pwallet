package com.example.pwallet.domain.security

import com.example.pwallet.domain.model.normalizedForSearch

object PinPolicy {
    const val LENGTH = 4

    fun formatError(pin: String): String? =
        if (pin.length == LENGTH && pin.all(Char::isDigit)) null else "El PIN debe tener $LENGTH dígitos"

    fun mismatchError(pin: String, confirm: String): String? =
        if (pin == confirm) null else "Los PIN no coinciden"

    fun keep(input: String): String = input.filter(Char::isDigit).take(LENGTH)
}


fun String.normalizedAnswer(): String = normalizedForSearch().replace(Regex("\\s+"), " ")