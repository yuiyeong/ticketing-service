package com.yuiyeong.ticketing.domain.message.payment

import com.yuiyeong.ticketing.domain.event.payment.PaymentEvent
import java.math.BigDecimal

sealed class PaymentMessage {
    abstract val userId: Long
    abstract val reservationId: Long
    abstract val paymentId: Long
    abstract val amount: BigDecimal
    abstract val publishedTimeMilli: Long

    data class Success(
        override val userId: Long,
        override val reservationId: Long,
        override val paymentId: Long,
        override val amount: BigDecimal,
        override val publishedTimeMilli: Long,
    ) : PaymentMessage()

    data class Failure(
        override val userId: Long,
        override val reservationId: Long,
        override val paymentId: Long,
        override val amount: BigDecimal,
        override val publishedTimeMilli: Long,
        val failureReason: String?,
    ) : PaymentMessage()

    companion object {
        fun createFrom(event: PaymentEvent): PaymentMessage =
            if (event.failureReason == null) {
                Success(
                    userId = event.userId,
                    reservationId = event.reservationId,
                    paymentId = event.paymentId,
                    amount = event.amount,
                    publishedTimeMilli = event.publishedTimeMilli,
                )
            } else {
                Failure(
                    userId = event.userId,
                    reservationId = event.reservationId,
                    paymentId = event.paymentId,
                    amount = event.amount,
                    publishedTimeMilli = event.publishedTimeMilli,
                    failureReason = event.failureReason,
                )
            }
    }
}
