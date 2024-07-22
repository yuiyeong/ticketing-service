package com.yuiyeong.ticketing.domain.model.payment

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import java.math.BigDecimal
import java.time.ZonedDateTime

class Payment(
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
    fun copy(
        id: Long = this.id,
        userId: Long = this.userId,
        transactionId: Long? = this.transactionId,
        reservationId: Long = this.reservationId,
        amount: BigDecimal = this.amount,
        status: PaymentStatus = this.status,
        paymentMethod: PaymentMethod = this.paymentMethod,
        failureReason: String? = this.failureReason,
        createdAt: ZonedDateTime = this.createdAt,
        updatedAt: ZonedDateTime = this.updatedAt,
    ): Payment =
        Payment(
            id = id,
            userId = userId,
            transactionId = transactionId,
            reservationId = reservationId,
            amount = amount,
            status = status,
            paymentMethod = paymentMethod,
            failureReason = failureReason,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

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
