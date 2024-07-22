package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.payment.PaymentResult
import com.yuiyeong.ticketing.domain.service.payment.PaymentService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetPaymentListUseCaseImpl(
    private val paymentService: PaymentService,
) : GetPaymentListUseCase {
    @Transactional(readOnly = true)
    override fun execute(userId: Long): List<PaymentResult> = paymentService.getHistory(userId).map { PaymentResult.from(it) }
}
