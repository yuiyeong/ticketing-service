package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.common.asUtc
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Transaction(
    val id: Long,
    val walletId: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun create(
            wallet: Wallet,
            amount: BigDecimal,
            type: TransactionType,
        ): Transaction =
            Transaction(
                id = 0L,
                walletId = wallet.id,
                amount = amount,
                type = type,
                createdAt = ZonedDateTime.now().asUtc,
            )
    }
}

enum class TransactionType {
    CHARGE,
    PAYMENT,
}
