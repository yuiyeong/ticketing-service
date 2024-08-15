package com.yuiyeong.ticketing.application.usecase.notification

import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage

/**
 * 결제 알림을 보내는 UseCase
 */
interface SendPaymentNotificationUseCase {
    fun execute(paymentMessage: PaymentMessage)
}
