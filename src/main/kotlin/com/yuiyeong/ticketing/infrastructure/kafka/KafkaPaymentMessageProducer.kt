package com.yuiyeong.ticketing.infrastructure.kafka

import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessageProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaPaymentMessageProducer(
    @Value("\${config.kafka.topic.payment}") private val paymentTopic: String,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaKeyGenerator: KafkaKeyGenerator,
) : PaymentMessageProducer {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java.simpleName)

    override fun send(paymentMessage: PaymentMessage) {
        val key = kafkaKeyGenerator.generateFrom(paymentMessage)
        kafkaTemplate.send(paymentTopic, key, paymentMessage)
        logger.info("PaymentMessage(published at ${paymentMessage.publishedTimeMilli}) is sent.")
    }
}
