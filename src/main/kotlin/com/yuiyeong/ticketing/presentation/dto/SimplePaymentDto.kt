package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.PaymentDto
import java.math.BigDecimal
import java.time.ZonedDateTime

data class SimplePaymentDto(
    val id: Long,
    val reservationId: Long,
    val amount: BigDecimal,
    val status: String,
    val paidAt: ZonedDateTime,
) {
    companion object {
        fun from(paymentDto: PaymentDto): SimplePaymentDto =
            SimplePaymentDto(
                paymentDto.id,
                paymentDto.reservationId,
                paymentDto.amount,
                paymentDto.status.toString(),
                paymentDto.createdAt,
            )
    }
}
