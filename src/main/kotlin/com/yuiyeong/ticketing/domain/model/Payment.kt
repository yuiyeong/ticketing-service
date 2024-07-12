package com.yuiyeong.ticketing.domain.model

import java.math.BigDecimal
import java.time.ZonedDateTime

data class Payment(
    val id: Long = 0L,
    val userId: Long,
    val transactionId: Long?,
    val reservationId: Long,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val failureReason: String?,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    companion object {
        fun create(
            userId: Long,
            reservation: Reservation,
            transaction: Transaction?,
            failureReason: String?,
        ): Payment {
            val now = ZonedDateTime.now()
            return Payment(
                userId = userId,
                transactionId = transaction?.id,
                reservationId = reservation.id,
                amount = reservation.totalAmount,
                status = if (failureReason != null) PaymentStatus.COMPLETED else PaymentStatus.FAILED,
                paymentMethod = PaymentMethod.Wallet,
                failureReason = failureReason,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
}

enum class PaymentMethod {
    Wallet,
}
