package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.payment.PaymentResult

interface PayUseCase {
    fun execute(
        userId: Long,
        token: String,
        reservationId: Long,
    ): PaymentResult
}
