package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.PaymentResult
import com.yuiyeong.ticketing.domain.service.PaymentService

class PaymentListUseCaseImpl(
    private val paymentService: PaymentService,
) : PaymentListUseCase {
    override fun getHistory(userId: Long): List<PaymentResult> = paymentService.getHistory(userId).map { PaymentResult.from(it) }
}
