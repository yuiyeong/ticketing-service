package com.yuiyeong.ticketing.domain.model

import java.math.BigDecimal
import java.time.ZonedDateTime

data class Transaction(
    val id: Long = 0L,
    val walletId: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val createdAt: ZonedDateTime,
)

enum class TransactionType {
    CHARGE,
    PAYMENT,
}
