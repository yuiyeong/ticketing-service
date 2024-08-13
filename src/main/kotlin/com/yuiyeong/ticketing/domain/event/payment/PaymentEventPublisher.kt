package com.yuiyeong.ticketing.domain.event.payment

/**
 * 결제 관련 이벤트를 발행하는 Publisher
 */
interface PaymentEventPublisher {
    fun publish(event: PaymentEvent)
}
