package com.example.pwallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.pwallet.domain.model.AuditAction
import com.example.pwallet.domain.model.CloseReason
import com.example.pwallet.domain.model.LoanStatus
import com.example.pwallet.domain.model.PaymentFrequency
import java.time.LocalDate

@Entity(tableName = "audit_log", indices = [Index("timestamp")])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val userId: String?,
    val action: AuditAction,
    val details: String?,
)

@Entity(
    tableName = "clients",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("ownerId"), Index("searchName")],
)
data class ClientEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val firstName: String,
    val lastName1: String,
    val lastName2: String,
    val nationalId: String?,
    val email: String?,
    val phone: String,
    val searchName: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("clientId"), Index("status")],
)
data class LoanEntity(
    @PrimaryKey val id: String,
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
    val closedAt: Long?,
    val closeReason: CloseReason?,
    val closeNote: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["loanId", "number"], unique = true), Index("dueDate")],
)
data class InstallmentEntity(
    @PrimaryKey val id: String,
    val loanId: String,
    val number: Int,
    val dueDate: LocalDate,
    val amountDue: Long,
    val amountPaid: Long,
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["loanId", "paymentDate"]), Index("createdBy")],
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val loanId: String,
    val paymentDate: LocalDate,
    val amountReceived: Long,
    val amountApplied: Long,
    val balanceAfter: Long,
    val note: String?,
    val createdBy: String,
    val createdAt: Long,
)
