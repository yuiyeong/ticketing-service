package com.yuiyeong.ticketing.domain.model.payment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yuiyeong.ticketing.domain.event.payment.PaymentEvent

data class PaymentOutbox(
    val id: Long,
    val paymentId: Long,
    val payload: String,
    val status: PaymentOutboxStatus,
    val publishedTimeMilli: Long,
) {
    fun markAsPublished(): PaymentOutbox = copy(status = PaymentOutboxStatus.PUBLISHED)

    fun extractPaymentEvent(): PaymentEvent = objectMapper.readValue(payload, PaymentEvent::class.java)

    companion object {
        private val objectMapper = jacksonObjectMapper()

        fun createFrom(paymentEvent: PaymentEvent): PaymentOutbox =
            PaymentOutbox(
                id = 0L,
                paymentId = paymentEvent.paymentId,
                payload = objectMapper.writeValueAsString(paymentEvent),
                status = PaymentOutboxStatus.CREATED,
                publishedTimeMilli = paymentEvent.publishedTimeMilli,
            )
    }
}

enum class PaymentOutboxStatus {
    CREATED,
    PUBLISHED,
}
