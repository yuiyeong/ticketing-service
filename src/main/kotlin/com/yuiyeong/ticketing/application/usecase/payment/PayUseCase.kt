package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.PaymentResult

interface PayUseCase {
    fun execute(
        userId: Long,
        queueEntryId: Long,
        reservationId: Long,
    ): PaymentResult
}
