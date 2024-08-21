package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.domain.service.payment.PaymentOutboxService
import org.springframework.stereotype.Component

@Component
class MarkPaymentOutboxAsPublishedUseCaseImpl(
    private val paymentOutboxService: PaymentOutboxService,
) : MarkPaymentOutboxAsPublishedUseCase {
    override fun execute(paymentId: Long) {
        paymentOutboxService.markAsPublishedIfExistByPaymentId(paymentId)
    }
}
