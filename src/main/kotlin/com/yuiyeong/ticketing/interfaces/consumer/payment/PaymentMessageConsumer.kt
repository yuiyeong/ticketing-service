package com.yuiyeong.ticketing.interfaces.consumer.payment

import com.yuiyeong.ticketing.application.usecase.notification.SendPaymentNotificationUseCase
import com.yuiyeong.ticketing.application.usecase.payment.MarkPaymentOutboxAsPublishedUseCase
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PaymentMessageConsumer(
    private val markPaymentOutboxAsPublishedUseCase: MarkPaymentOutboxAsPublishedUseCase,
    private val sendPaymentNotificationUseCase: SendPaymentNotificationUseCase,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    /**
     * PaymentOutbox 의 마킹 처리용 listener
     */
    @KafkaListener(
        topics = ["\${config.kafka.topic.payment}"],
        groupId = "\${config.kafka.consumer-group-id.payment-outbox}",
    )
    fun markPaymentOutboxAsPublishedBy(paymentMessage: PaymentMessage) {
        logger.info("Marking PaymentOutbox of paymentMessage(at ${paymentMessage.publishedTimeMilli}).")

        markPaymentOutboxAsPublishedUseCase.execute(paymentMessage.paymentId)
    }

    /**
     * PaymentMessage 를 소비하여, 비지니스 로직 수행
     */
    @KafkaListener(
        topics = ["\${config.kafka.topic.payment}"],
        groupId = "\${config.kafka.consumer-group-id.payment}",
    )
    fun consumePaymentMessage(paymentMessage: PaymentMessage) {
        logger.info("Consuming paymentMessage(at ${paymentMessage.publishedTimeMilli}).")

        sendPaymentNotificationUseCase.execute(paymentMessage)
    }
}
