package com.yuiyeong.ticketing.unit.domain.model.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutbox
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.helper.TestDataFactory.createRandomPaymentEvent
import org.assertj.core.api.Assertions
import kotlin.test.Test

class PaymentOutboxTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `should return a PaymentOutbox from a PaymentEvent`() {
        // given
        val paymentEvent = createRandomPaymentEvent()

        // when
        val paymentOutbox = PaymentOutbox.createFrom(paymentEvent)

        // then
        Assertions.assertThat(paymentOutbox.payload).isEqualTo(objectMapper.writeValueAsString(paymentEvent))
        Assertions.assertThat(paymentOutbox.publishedTimeMilli).isEqualTo(paymentEvent.publishedTimeMilli)
        Assertions.assertThat(paymentOutbox.status).isEqualTo(PaymentOutboxStatus.CREATED)
    }

    @Test
    fun `should return a paymentEvent from a PaymentOutbox that is created by the event`() {
        // given
        val paymentEvent = createRandomPaymentEvent("testFailureReason")
        val paymentOutbox = PaymentOutbox.createFrom(paymentEvent)

        // when
        val extractedPaymentEvent = paymentOutbox.extractPaymentEvent()

        // then
        Assertions.assertThat(extractedPaymentEvent.userId).isEqualTo(paymentEvent.userId)
        Assertions.assertThat(extractedPaymentEvent.reservationId).isEqualTo(paymentEvent.reservationId)
        Assertions.assertThat(extractedPaymentEvent.amount).isEqualByComparingTo(paymentEvent.amount)
        Assertions.assertThat(extractedPaymentEvent.failureReason).isEqualTo(paymentEvent.failureReason)
        Assertions.assertThat(extractedPaymentEvent.publishedTimeMilli).isEqualTo(paymentEvent.publishedTimeMilli)
    }

    @Test
    fun `should return published payment outbox after markAsPublished`() {
        // given
        val paymentEvent = createRandomPaymentEvent()
        val paymentOutbox = PaymentOutbox.createFrom(paymentEvent)

        // when
        val outbox = paymentOutbox.markAsPublished()

        // then
        Assertions.assertThat(outbox.status).isEqualTo(PaymentOutboxStatus.PUBLISHED)
    }
}
