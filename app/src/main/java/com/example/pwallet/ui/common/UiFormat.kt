package com.example.pwallet.ui.common

import com.example.pwallet.domain.model.PaymentFrequency
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.toDisplay(): String = format(DATE_FORMAT)

fun PaymentFrequency.label(): String = when (this) {
    PaymentFrequency.WEEKLY -> "Semanal"
    PaymentFrequency.MONTHLY -> "Mensual"
    PaymentFrequency.QUARTERLY -> "Trimestral"
}