package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Payment
import com.yuiyeong.ticketing.domain.model.PaymentStatus
import java.math.BigDecimal
import java.time.ZonedDateTime

data class PaymentResult(
    val id: Long,
    val reservationId: Long,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val failureReason: String?,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(payment: Payment): PaymentResult =
            PaymentResult(
                payment.id,
                payment.reservationId,
                payment.amount,
                payment.status,
                payment.failureReason,
                payment.createdAt,
            )
    }
}
