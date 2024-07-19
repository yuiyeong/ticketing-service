package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.InsufficientBalanceException
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
    fun charge(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            throw InvalidAmountException()
        }
        balance += amount
    }

    fun pay(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            throw InvalidAmountException()
        }

        if (balance < amount) {
            throw InsufficientBalanceException()
        }

        balance -= amount
    }

    companion object {
        fun create(userId: Long): Wallet {
            val now = ZonedDateTime.now().asUtc
            return Wallet(
                id = 0L,
                userId = userId,
                balance = BigDecimal.ZERO,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
