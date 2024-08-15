package com.yuiyeong.ticketing.domain.repository.payment

import com.yuiyeong.ticketing.domain.model.payment.PaymentOutbox
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus

interface PaymentOutboxRepository {
    fun save(paymentOutbox: PaymentOutbox): PaymentOutbox

    fun saveAll(paymentOutboxes: List<PaymentOutbox>): List<PaymentOutbox>

    fun findOneByPaymentIdAndStatus(
        paymentId: Long,
        status: PaymentOutboxStatus,
    ): PaymentOutbox?

    fun findAllByStatusAndPublishedTimeMilliBefore(
        status: PaymentOutboxStatus,
        momentTimeMilli: Long,
    ): List<PaymentOutbox>
}
