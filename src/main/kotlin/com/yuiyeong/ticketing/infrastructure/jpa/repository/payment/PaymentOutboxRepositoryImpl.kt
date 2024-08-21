package com.yuiyeong.ticketing.infrastructure.jpa.repository.payment

import com.yuiyeong.ticketing.domain.model.payment.PaymentOutbox
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import com.yuiyeong.ticketing.infrastructure.jpa.entity.payment.PaymentOutboxEntity
import com.yuiyeong.ticketing.infrastructure.jpa.entity.payment.PaymentOutboxEntityStatus
import org.springframework.stereotype.Repository

@Repository
class PaymentOutboxRepositoryImpl(
    private val paymentOutboxJpaRepository: PaymentOutboxJpaRepository,
) : PaymentOutboxRepository {
    override fun save(paymentOutbox: PaymentOutbox): PaymentOutbox =
        paymentOutboxJpaRepository.save(PaymentOutboxEntity.from(paymentOutbox)).toPaymentOutbox()

    override fun saveAll(paymentOutboxes: List<PaymentOutbox>): List<PaymentOutbox> =
        paymentOutboxJpaRepository
            .saveAll(
                paymentOutboxes.map {
                    PaymentOutboxEntity.from(it)
                },
            ).map { it.toPaymentOutbox() }

    override fun findOneByPaymentIdAndStatus(
        paymentId: Long,
        status: PaymentOutboxStatus,
    ): PaymentOutbox? =
        paymentOutboxJpaRepository
            .findByPaymentIdAndStatus(
                paymentId,
                PaymentOutboxEntityStatus.from(status),
            )?.toPaymentOutbox()

    override fun findAllByStatusAndPublishedTimeMilliBefore(
        status: PaymentOutboxStatus,
        momentTimeMilli: Long,
    ): List<PaymentOutbox> =
        paymentOutboxJpaRepository
            .findByStatusAndPublishedTimeMilliBefore(
                PaymentOutboxEntityStatus.from(status),
                momentTimeMilli,
            ).map { it.toPaymentOutbox() }
}
