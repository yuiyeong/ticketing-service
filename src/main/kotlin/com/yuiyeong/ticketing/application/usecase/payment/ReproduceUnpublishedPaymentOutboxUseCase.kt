package com.yuiyeong.ticketing.application.usecase.payment

/**
 * 특정 시점 이전에 발행 시도한 PaymentOutbox 중,
 * PUBLISHED 가 아닌 PaymentOutbox 를 재발행하는 UseCase
 */
interface ReproduceUnpublishedPaymentOutboxUseCase {
    fun execute(): Int
}
