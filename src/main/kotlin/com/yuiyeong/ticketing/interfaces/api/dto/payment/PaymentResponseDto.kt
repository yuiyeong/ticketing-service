package com.yuiyeong.ticketing.interfaces.api.dto.payment

import com.yuiyeong.ticketing.application.dto.payment.PaymentResult
import java.math.BigDecimal
import java.time.ZonedDateTime

data class PaymentResponseDto(
    val id: Long,
    val reservationId: Long,
    val amount: BigDecimal,
    val status: String,
    val paidAt: ZonedDateTime,
) {
    companion object {
        fun from(paymentResult: PaymentResult): PaymentResponseDto =
            PaymentResponseDto(
                paymentResult.id,
                paymentResult.reservationId,
                paymentResult.amount,
                paymentResult.status.toString(),
                paymentResult.createdAt,
            )
    }
}
