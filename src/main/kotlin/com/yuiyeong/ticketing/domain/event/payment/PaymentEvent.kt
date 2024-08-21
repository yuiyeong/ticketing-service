package com.yuiyeong.ticketing.domain.event.payment

import com.yuiyeong.ticketing.domain.model.payment.Payment
import java.math.BigDecimal

data class PaymentEvent(
    val userId: Long,
    val reservationId: Long,
    val paymentId: Long,
    val amount: BigDecimal,
    val failureReason: String?,
    val publishedTimeMilli: Long,
) {
    companion object {
        fun create(payment: Payment): PaymentEvent =
            PaymentEvent(
                userId = payment.userId,
                reservationId = payment.reservationId,
                paymentId = payment.id,
                amount = payment.amount,
                failureReason = payment.failureReason,
                publishedTimeMilli = System.currentTimeMillis(),
            )
    }
}
