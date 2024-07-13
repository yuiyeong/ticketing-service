package com.yuiyeong.ticketing.domain.model

import java.math.BigDecimal
import java.time.ZonedDateTime

data class Transaction(
    val id: Long = 0L,
    val walletId: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun createAsCharge(
            wallet: Wallet,
            amount: BigDecimal,
        ): Transaction = create(wallet, amount, TransactionType.CHARGE)

        fun createAsPayment(
            wallet: Wallet,
            amount: BigDecimal,
        ): Transaction = create(wallet, amount, TransactionType.PAYMENT)

        fun create(
            wallet: Wallet,
            amount: BigDecimal,
            type: TransactionType,
        ): Transaction =
            Transaction(
                walletId = wallet.id,
                amount = amount,
                type = type,
                createdAt = wallet.updatedAt,
            )
    }
}

enum class TransactionType {
    CHARGE,
    PAYMENT,
}
