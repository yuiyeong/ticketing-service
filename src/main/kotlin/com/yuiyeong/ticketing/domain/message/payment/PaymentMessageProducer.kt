package com.yuiyeong.ticketing.domain.message.payment

/**
 * PaymentMessage 를 MessageQueue 로 보내는 producer
 */
interface PaymentMessageProducer {
    fun send(paymentMessage: PaymentMessage)
}
