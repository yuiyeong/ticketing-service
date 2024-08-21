package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessageProducer
import com.yuiyeong.ticketing.domain.service.payment.PaymentOutboxService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class ReproduceUnpublishedPaymentOutboxUseCaseImpl(
    @Value("\${config.outbox.threshold-as-min}") private val outboxThresholdAsMin: Long,
    private val paymentOutboxService: PaymentOutboxService,
    private val paymentMessageProducer: PaymentMessageProducer,
) : ReproduceUnpublishedPaymentOutboxUseCase {
    override fun execute(): Int {
        val moment = ZonedDateTime.now().minusMinutes(outboxThresholdAsMin).asUtc
        val unpublishedOutboxes = paymentOutboxService.findUnpublishedOutboxesBefore(moment)

        unpublishedOutboxes
            .map { it.extractPaymentEvent() }
            .map { PaymentMessage.createFrom(it) }
            .forEach { paymentMessageProducer.send(it) }

        return unpublishedOutboxes.size
    }
}
