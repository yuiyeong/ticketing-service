package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.payment.PaymentResult
import com.yuiyeong.ticketing.domain.service.payment.PaymentService
import org.springframework.stereotype.Component

@Component
class GetPaymentListUseCaseImpl(
    private val paymentService: PaymentService,
) : GetPaymentListUseCase {
    override fun execute(userId: Long): List<PaymentResult> = paymentService.getHistory(userId).map { PaymentResult.from(it) }
}
