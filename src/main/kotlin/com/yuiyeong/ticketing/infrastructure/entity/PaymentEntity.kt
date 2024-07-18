package com.yuiyeong.ticketing.infrastructure.entity

import com.yuiyeong.ticketing.domain.model.Payment
import com.yuiyeong.ticketing.domain.model.PaymentMethod
import com.yuiyeong.ticketing.domain.model.PaymentStatus
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "payment")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userId: Long,
    val transactionId: Long?,
    val reservationId: Long,
    val amount: BigDecimal,
    val paymentMethod: PaymentEntityMethod,
    val status: PaymentEntityStatus,
    val failureReason: String?,
) : BaseEntity() {
    fun toPayment(): Payment =
        Payment(
            id = id,
            userId = userId,
            transactionId = transactionId,
            reservationId = reservationId,
            amount = amount,
            status = status.toPaymentStatus(),
            paymentMethod = paymentMethod.toPaymentMethod(),
            failureReason = failureReason,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    companion object {
        fun from(payment: Payment): PaymentEntity =
            PaymentEntity(
                id = payment.id,
                userId = payment.userId,
                transactionId = payment.transactionId,
                reservationId = payment.reservationId,
                amount = payment.amount,
                paymentMethod = PaymentEntityMethod.from(payment.paymentMethod),
                status = PaymentEntityStatus.from(payment.status),
                failureReason = payment.failureReason,
            )
    }
}

enum class PaymentEntityStatus {
    PENDING,
    SUCCESS,
    FAILED,
    ;

    fun toPaymentStatus(): PaymentStatus =
        when (this) {
            PENDING -> PaymentStatus.PENDING
            SUCCESS -> PaymentStatus.COMPLETED
            FAILED -> PaymentStatus.FAILED
        }

    companion object {
        fun from(paymentStatus: PaymentStatus): PaymentEntityStatus =
            when (paymentStatus) {
                PaymentStatus.PENDING -> PENDING
                PaymentStatus.COMPLETED -> SUCCESS
                PaymentStatus.FAILED -> FAILED
            }
    }
}

enum class PaymentEntityMethod {
    WALLET,
    ;

    fun toPaymentMethod(): PaymentMethod =
        when (this) {
            WALLET -> PaymentMethod.WALLET
        }

    companion object {
        fun from(paymentMethod: PaymentMethod): PaymentEntityMethod =
            when (paymentMethod) {
                PaymentMethod.WALLET -> WALLET
            }
    }
}
