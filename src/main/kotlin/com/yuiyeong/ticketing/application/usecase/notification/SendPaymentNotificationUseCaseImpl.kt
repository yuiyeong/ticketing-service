package com.yuiyeong.ticketing.application.usecase.notification

import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import com.yuiyeong.ticketing.domain.notification.PaymentNotificationService
import org.springframework.stereotype.Component

@Component
class SendPaymentNotificationUseCaseImpl(
    private val paymentNotificationService: PaymentNotificationService,
) : SendPaymentNotificationUseCase {
    override fun execute(paymentMessage: PaymentMessage) {
        paymentNotificationService.notifyPaymentResult(paymentMessage)
    }
}
