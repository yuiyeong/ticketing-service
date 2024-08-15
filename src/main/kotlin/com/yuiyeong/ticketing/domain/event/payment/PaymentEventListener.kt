package com.yuiyeong.ticketing.domain.event.payment

import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessageProducer
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutbox
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(
    private val paymentOutboxRepository: PaymentOutboxRepository,
    private val paymentMessageProducer: PaymentMessageProducer,
) {
    /**
     * paymentEvent 발행시 그 이벤트에 대한 PaymentOutbox 를 생성할 수 있도록 하는 함수
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun createPaymentOutboxFrom(paymentEvent: PaymentEvent) {
        paymentOutboxRepository.save(PaymentOutbox.createFrom(paymentEvent))
    }

    /**
     * 결제에 대한 transaction 이 온전히 완료되었을 때, paymentEvent 로 부터 PaymentMessage 를 발행하는 함수
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun producePaymentMessageFrom(paymentEvent: PaymentEvent) {
        paymentMessageProducer.send(PaymentMessage.createFrom(paymentEvent))
    }
}
