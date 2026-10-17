package com.example.pwallet.domain.usecase

import com.example.pwallet.domain.model.PaymentFrequency
import java.time.LocalDate


data class LoanTerms(
    val principal: Long,
    val interestRateBps: Int,
    val installmentsCount: Int,
    val frequency: PaymentFrequency,
    val loanDate: LocalDate,
    val firstDueDate: LocalDate? = null,
)

data class PlannedInstallment(val number: Int, val dueDate: LocalDate, val amount: Long)

data class LoanPlan(
    val principal: Long,
    val interest: Long,
    val total: Long,
    val installments: List<PlannedInstallment>,
)

object LoanCalculator {
    private const val BPS = 10_000L

    fun calculate(t: LoanTerms): LoanPlan {
        require(t.principal > 0) { "El capital debe ser mayor a 0" }
        require(t.interestRateBps >= 0) { "El interés no puede ser negativo" }
        require(t.installmentsCount >= 1) { "Debe haber al menos 1 cuota" }
        t.firstDueDate?.let { require(!it.isBefore(t.loanDate)) { "El primer pago no puede ser anterior al préstamo" } }

        val interest = (Math.multiplyExact(t.principal, t.interestRateBps.toLong()) + BPS / 2) / BPS
        val total = Math.addExact(t.principal, interest)

        val base = total / t.installmentsCount
        val remainder = (total % t.installmentsCount).toInt()
        val items = (1..t.installmentsCount).map { n ->
            PlannedInstallment(n, dueDate(t, n), base + if (n <= remainder) 1 else 0)
        }
        return LoanPlan(t.principal, interest, total, items)
    }

    private fun dueDate(t: LoanTerms, n: Int): LocalDate =
        t.firstDueDate?.let { t.frequency.advance(it, (n - 1).toLong()) }
            ?: t.frequency.advance(t.loanDate, n.toLong())
}