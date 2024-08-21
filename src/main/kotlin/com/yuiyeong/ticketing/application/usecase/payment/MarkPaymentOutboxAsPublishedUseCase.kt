package com.yuiyeong.ticketing.application.usecase.payment

/**
 * paymentId 를 갖는 PaymentOutbox 를 PUBLISHED 로 마킹하는 UseCase
 */
interface MarkPaymentOutboxAsPublishedUseCase {
    fun execute(paymentId: Long)
}
