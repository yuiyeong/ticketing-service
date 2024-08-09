package com.yuiyeong.ticketing.application.event

import com.yuiyeong.ticketing.domain.event.payment.PaymentEvent
import com.yuiyeong.ticketing.domain.notification.PaymentNotificationService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventHandler(
    private val paymentNotificationService: PaymentNotificationService,
) {
    /**
     * PaymentEvent 를 받아서 알림을 보내는 함수
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PaymentEvent) {
        paymentNotificationService.notifyPaymentResult(event)
    }
}
