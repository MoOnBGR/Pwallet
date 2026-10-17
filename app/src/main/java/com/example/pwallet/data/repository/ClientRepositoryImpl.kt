package com.example.pwallet.data.repository

import androidx.room.withTransaction
import com.example.pwallet.data.local.AppDatabase
import com.example.pwallet.data.local.entity.ClientEntity
import com.example.pwallet.data.local.entity.InstallmentEntity
import com.example.pwallet.data.local.entity.LoanEntity
import com.example.pwallet.domain.model.Client
import com.example.pwallet.domain.model.Installment
import com.example.pwallet.domain.model.Loan
import com.example.pwallet.domain.model.normalizedForSearch
import com.example.pwallet.domain.repository.ClientRepository

class ClientRepositoryImpl(private val db: AppDatabase) : ClientRepository {

    override suspend fun createClientWithLoan(client: Client, loan: Loan, installments: List<Installment>) {
        db.withTransaction {
            db.clientDao().insert(client.toEntity())
            db.loanDao().insert(loan.toEntity())
            db.installmentDao().insertAll(installments.map { it.toEntity() })
        }
    }
}

private fun Client.toEntity() = ClientEntity(
    id = id,
    ownerId = ownerId,
    firstName = firstName,
    lastName1 = lastName1,
    lastName2 = lastName2,
    nationalId = nationalId,
    email = email,
    phone = phone,
    searchName = fullName.normalizedForSearch(),
    createdAt = createdAt,
    updatedAt = createdAt,
)

private fun Loan.toEntity() = LoanEntity(
    id = id,
    clientId = clientId,
    principal = principal,
    interestRateBps = interestRateBps,
    interestAmount = interestAmount,
    totalAmount = totalAmount,
    installmentsCount = installmentsCount,
    frequency = frequency,
    loanDate = loanDate,
    firstDueDate = firstDueDate,
    collateral = collateral,
    status = status,
    closedAt = null,
    closeReason = null,
    closeNote = null,
    createdAt = createdAt,
    updatedAt = createdAt,
)

private fun Installment.toEntity() = InstallmentEntity(
    id = id,
    loanId = loanId,
    number = number,
    dueDate = dueDate,
    amountDue = amountDue,
    amountPaid = amountPaid,
)