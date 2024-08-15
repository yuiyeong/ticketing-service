package com.yuiyeong.ticketing.infrastructure.slack

import com.slack.api.Slack
import com.yuiyeong.ticketing.config.SlackProperties
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import com.yuiyeong.ticketing.domain.notification.PaymentNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlackPaymentNotificationService(
    private val slackProperties: SlackProperties,
    private val slackPayloadCreator: SlackPayloadCreator,
) : PaymentNotificationService {
    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    private val slack = Slack.getInstance()

    override fun notifyPaymentResult(paymentMessage: PaymentMessage) {
        val payload = slackPayloadCreator.createPayloadFrom(paymentMessage)
        try {
            slack.send(slackProperties.webhookUrl, payload)
        } catch (ex: Exception) {
            logger.warn("Exception occurred while trying to send notification", ex)
        }
    }
}
