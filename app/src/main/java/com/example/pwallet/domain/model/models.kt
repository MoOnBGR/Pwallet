package com.example.pwallet.domain.model
import java.time.LocalDate

enum class Currency(val code: String, val symbol: String, val label: String) {
    USD("USD", "$", "Dólares"),
    CRC("CRC", "₡", "Colones"),
    NIO("NIO", "C$", "Córdobas"),
}


data class User(
    val id: String,
    val fullName: String,
    val phone: String,
    val currency: Currency,
    val businessName: String,
)

enum class PaymentFrequency {
    WEEKLY, MONTHLY, QUARTERLY;

    fun advance(from: LocalDate, steps: Long): LocalDate = when (this) {
        WEEKLY -> from.plusWeeks(steps)
        MONTHLY -> from.plusMonths(steps)
        QUARTERLY -> from.plusMonths(3 * steps)
    }
}

enum class LoanStatus { ACTIVE, CLOSED }

enum class CloseReason { PAID, INCOMPLETE }

data class Client(
    val id: String,
    val ownerId: String,
    val firstName: String,
    val lastName1: String,
    val lastName2: String,
    val nationalId: String?,
    val email: String?,
    val phone: String,
    val createdAt: Long,
) {
    val fullName: String get() = "$firstName $lastName1 $lastName2"
}

data class Loan(
    val id: String,
    val clientId: String,
    val principal: Long,
    val interestRateBps: Int,
    val interestAmount: Long,
    val totalAmount: Long,
    val installmentsCount: Int,
    val frequency: PaymentFrequency,
    val loanDate: LocalDate,
    val firstDueDate: LocalDate,
    val collateral: String?,
    val status: LoanStatus,
    val createdAt: Long,
)

data class Installment(
    val id: String,
    val loanId: String,
    val number: Int,
    val dueDate: LocalDate,
    val amountDue: Long,
    val amountPaid: Long,
)


class Session {
    @Volatile var user: User? = null
}
enum class AuditAction {
    USER_CREATED, LOGIN_SUCCESS, LOGIN_FAILED, PIN_RESET, RECOVERY_FAILED,
    CLIENT_CREATED, LOAN_CREATED,
}