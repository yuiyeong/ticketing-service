package com.yuiyeong.ticketing.infrastructure.kafka

import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import org.springframework.stereotype.Component

@Component
class KafkaKeyGenerator {
    /**
     * paymentMessage 의 property 로 부터 key 를 생성
     */
    fun generateFrom(paymentMessage: PaymentMessage): String =
        "user:${paymentMessage.userId}:reservation:${paymentMessage.reservationId}:payment:${paymentMessage.paymentId}"
}
