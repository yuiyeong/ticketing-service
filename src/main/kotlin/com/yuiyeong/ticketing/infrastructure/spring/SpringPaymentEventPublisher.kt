package com.yuiyeong.ticketing.infrastructure.spring

import com.yuiyeong.ticketing.domain.event.payment.PaymentEvent
import com.yuiyeong.ticketing.domain.event.payment.PaymentEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringPaymentEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : PaymentEventPublisher {
    override fun publish(event: PaymentEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
