package com.yuiyeong.ticketing.infrastructure.jpa.entity.payment

import com.yuiyeong.ticketing.domain.model.payment.PaymentOutbox
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.infrastructure.jpa.entity.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "payment_outbox")
class PaymentOutboxEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val paymentId: Long,
    @Column(columnDefinition = "TEXT")
    val payload: String,
    val publishedTimeMilli: Long,
    @Enumerated(EnumType.STRING)
    val status: PaymentOutboxEntityStatus,
    @Embedded
    val auditable: Auditable = Auditable(),
) {
    fun toPaymentOutbox(): PaymentOutbox =
        PaymentOutbox(
            id = id,
            paymentId = paymentId,
            payload = payload,
            status = status.toPaymentOutboxStatus(),
            publishedTimeMilli = publishedTimeMilli,
        )

    companion object {
        fun from(paymentOutbox: PaymentOutbox): PaymentOutboxEntity =
            PaymentOutboxEntity(
                id = paymentOutbox.id,
                paymentId = paymentOutbox.paymentId,
                payload = paymentOutbox.payload,
                publishedTimeMilli = paymentOutbox.publishedTimeMilli,
                status = PaymentOutboxEntityStatus.from(paymentOutbox.status),
            )
    }
}

enum class PaymentOutboxEntityStatus {
    CREATED,
    PUBLISHED,
    ;

    fun toPaymentOutboxStatus(): PaymentOutboxStatus =
        when (this) {
            CREATED -> PaymentOutboxStatus.CREATED
            PUBLISHED -> PaymentOutboxStatus.PUBLISHED
        }

    companion object {
        fun from(paymentOutboxStatus: PaymentOutboxStatus): PaymentOutboxEntityStatus =
            when (paymentOutboxStatus) {
                PaymentOutboxStatus.CREATED -> CREATED
                PaymentOutboxStatus.PUBLISHED -> PUBLISHED
            }
    }
}
