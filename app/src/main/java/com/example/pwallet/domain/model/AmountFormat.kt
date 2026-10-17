
package com.example.pwallet.domain.model

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.util.Locale

object AmountParser {
    private val MONEY = Regex("^\\d{1,12}(\\.\\d{1,2})?$")
    private val PERCENT = Regex("^\\d{1,3}(\\.\\d{1,2})?$")

    fun parseMinor(text: String): Long? {
        val clean = text.trim().replace(',', '.')
        if (!MONEY.matches(clean)) return null
        return BigDecimal(clean).movePointRight(2).longValueExact()
    }

    fun parseBps(text: String): Int? {
        val clean = text.trim().replace(',', '.')
        if (!PERCENT.matches(clean)) return null
        return BigDecimal(clean).movePointRight(2).toInt().takeIf { it <= 10_000 }
    }
}

fun formatMoney(minor: Long, currency: Currency): String {
    val format = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    return currency.symbol + format.format(BigDecimal.valueOf(minor, 2))
}

fun String.normalizedForSearch(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()