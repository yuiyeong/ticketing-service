package com.yuiyeong.ticketing.domain.model.payment

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Payment(
    val id: Long,
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
    val isFailed: Boolean
        get() = status == PaymentStatus.FAILED

    companion object {
        fun create(
            userId: Long,
            reservation: Reservation,
            transaction: Transaction?,
            failureReason: String?,
        ): Payment {
            val now = ZonedDateTime.now().asUtc
            return Payment(
                id = 0L,
                userId = userId,
                transactionId = transaction?.id,
                reservationId = reservation.id,
                amount = reservation.totalAmount,
                status = if (failureReason != null) PaymentStatus.FAILED else PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.WALLET,
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
    WALLET,
}
