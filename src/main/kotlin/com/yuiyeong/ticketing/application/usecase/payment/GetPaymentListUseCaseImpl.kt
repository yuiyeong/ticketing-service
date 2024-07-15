package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.PaymentResult
import com.yuiyeong.ticketing.domain.service.PaymentService

class GetPaymentListUseCaseImpl(
    private val paymentService: PaymentService,
) : GetPaymentListUseCase {
    override fun execute(userId: Long): List<PaymentResult> = paymentService.getHistory(userId).map { PaymentResult.from(it) }
}
