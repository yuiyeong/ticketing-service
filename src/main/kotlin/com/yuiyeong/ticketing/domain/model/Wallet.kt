package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.InvalidAmountException
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Wallet(
    val id: Long,
    val userId: Long,
    var balance: BigDecimal,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    fun charge(amount: Long) {
        if (amount <= 0) {
            throw InvalidAmountException()
        }
        balance += BigDecimal(amount)
    }
}