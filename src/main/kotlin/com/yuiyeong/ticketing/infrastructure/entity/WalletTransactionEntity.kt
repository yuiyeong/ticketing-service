package com.yuiyeong.ticketing.infrastructure.entity

import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.TransactionType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "wallet_transaction")
class WalletTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val walletId: Long,
    val amount: BigDecimal,
    @Enumerated(EnumType.STRING)
    val type: WalletTransactionType,
) : BaseEntity() {
    fun toTransaction(): Transaction =
        Transaction(
            id = this.id,
            walletId = this.walletId,
            amount = this.amount,
            type = this.type.toTransactionType(),
            createdAt = createdAt,
        )

    companion object {
        fun from(transaction: Transaction): WalletTransactionEntity =
            WalletTransactionEntity(
                id = transaction.id,
                walletId = transaction.walletId,
                amount = transaction.amount,
                type = WalletTransactionType.from(transaction.type),
            )
    }
}

enum class WalletTransactionType {
    CHARGED,
    PAID,
    ;

    fun toTransactionType(): TransactionType =
        when (this) {
            CHARGED -> TransactionType.CHARGE
            PAID -> TransactionType.PAYMENT
        }

    companion object {
        fun from(type: TransactionType): WalletTransactionType =
            when (type) {
                TransactionType.CHARGE -> CHARGED
                TransactionType.PAYMENT -> PAID
            }
    }
}
