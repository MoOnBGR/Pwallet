package com.example.pwallet.domain.usecase

import com.example.pwallet.domain.model.AmountParser
import com.example.pwallet.domain.model.AuditAction
import com.example.pwallet.domain.model.Client
import com.example.pwallet.domain.model.Installment
import com.example.pwallet.domain.model.Loan
import com.example.pwallet.domain.model.LoanStatus
import com.example.pwallet.domain.model.PaymentFrequency
import com.example.pwallet.domain.repository.AuditLogger
import com.example.pwallet.domain.repository.ClientRepository
import java.time.LocalDate
import java.util.UUID

data class ClientLoanForm(
    val firstName: String = "",
    val lastName1: String = "",
    val lastName2: String = "",
    val phone: String = "",
    val nationalId: String = "",
    val email: String = "",
    val collateral: String = "",
    val loanDate: LocalDate = LocalDate.now(),
    val principal: String = "",
    val interestPercent: String = "",
    val installments: String = "",
    val frequency: PaymentFrequency = PaymentFrequency.MONTHLY,
    val firstDueDate: LocalDate? = null,
)

enum class ClientField {
    FIRST_NAME, LAST_NAME_1, LAST_NAME_2, PHONE, EMAIL,
    PRINCIPAL, INTEREST, INSTALLMENTS, FIRST_DUE_DATE,
}

sealed interface CreateClientResult {
    data class Success(val clientId: String) : CreateClientResult
    data class Invalid(val errors: Map<ClientField, String>) : CreateClientResult
}

class CreateClientWithLoanUseCase(
    private val clients: ClientRepository,
    private val audit: AuditLogger,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) {

    fun preview(form: ClientLoanForm): LoanPlan? = termsOrNull(form)?.let(LoanCalculator::calculate)

    suspend operator fun invoke(ownerId: String, form: ClientLoanForm): CreateClientResult {
        val errors = validate(form)
        if (errors.isNotEmpty()) return CreateClientResult.Invalid(errors)

        val terms = checkNotNull(termsOrNull(form))
        val plan = LoanCalculator.calculate(terms)
        val timestamp = now()
        val clientId = newId()
        val loanId = newId()

        val client = Client(
            id = clientId,
            ownerId = ownerId,
            firstName = form.firstName.clean(),
            lastName1 = form.lastName1.clean(),
            lastName2 = form.lastName2.clean(),
            nationalId = form.nationalId.clean().ifEmpty { null },
            email = form.email.trim().lowercase().ifEmpty { null },
            phone = form.phone.filter(Char::isDigit),
            createdAt = timestamp,
        )
        val loan = Loan(
            id = loanId,
            clientId = clientId,
            principal = plan.principal,
            interestRateBps = terms.interestRateBps,
            interestAmount = plan.interest,
            totalAmount = plan.total,
            installmentsCount = terms.installmentsCount,
            frequency = terms.frequency,
            loanDate = terms.loanDate,
            firstDueDate = plan.installments.first().dueDate,
            collateral = form.collateral.clean().ifEmpty { null },
            status = LoanStatus.ACTIVE,
            createdAt = timestamp,
        )
        val installments = plan.installments.map {
            Installment(newId(), loanId, it.number, it.dueDate, it.amount, amountPaid = 0)
        }

        clients.createClientWithLoan(client, loan, installments)
        audit.log(AuditAction.CLIENT_CREATED, ownerId, """{"clientId":"$clientId"}""")
        audit.log(AuditAction.LOAN_CREATED, ownerId, """{"loanId":"$loanId","total":${plan.total}}""")
        return CreateClientResult.Success(clientId)
    }

    private fun validate(f: ClientLoanForm): Map<ClientField, String> = buildMap {
        if (f.firstName.isBlank()) put(ClientField.FIRST_NAME, "Obligatorio")
        if (f.lastName1.isBlank()) put(ClientField.LAST_NAME_1, "Obligatorio")
        if (f.lastName2.isBlank()) put(ClientField.LAST_NAME_2, "Obligatorio")
        if (f.phone.filter(Char::isDigit).length !in 8..15) put(ClientField.PHONE, "Número de celular inválido")
        if (f.email.isNotBlank() && !EMAIL.matches(f.email.trim())) put(ClientField.EMAIL, "Correo inválido")
        if (AmountParser.parseMinor(f.principal)?.let { it > 0 } != true) {
            put(ClientField.PRINCIPAL, "Monto inválido (ej. 1500.50)")
        }
        if (AmountParser.parseBps(f.interestPercent) == null) {
            put(ClientField.INTEREST, "Interés inválido (0 a 100, ej. 10 o 7.5)")
        }
        if (f.installments.trim().toIntOrNull() !in 1..MAX_INSTALLMENTS) {
            put(ClientField.INSTALLMENTS, "Entre 1 y $MAX_INSTALLMENTS cuotas")
        }
        if (f.firstDueDate?.isBefore(f.loanDate) == true) {
            put(ClientField.FIRST_DUE_DATE, "No puede ser anterior al préstamo")
        }
    }

    private fun termsOrNull(f: ClientLoanForm): LoanTerms? {
        val principal = AmountParser.parseMinor(f.principal)?.takeIf { it > 0 } ?: return null
        val bps = AmountParser.parseBps(f.interestPercent) ?: return null
        val count = f.installments.trim().toIntOrNull()?.takeIf { it in 1..MAX_INSTALLMENTS } ?: return null
        if (f.firstDueDate?.isBefore(f.loanDate) == true) return null
        return LoanTerms(principal, bps, count, f.frequency, f.loanDate, f.firstDueDate)
    }

    private fun String.clean() = trim().replace(Regex("\\s+"), " ")

    private companion object {
        const val MAX_INSTALLMENTS = 120
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}