package com.yuiyeong.ticketing.infrastructure.jpa.repository.payment

import com.yuiyeong.ticketing.domain.model.payment.Payment
import com.yuiyeong.ticketing.domain.repository.payment.PaymentRepository
import com.yuiyeong.ticketing.infrastructure.jpa.entity.payment.PaymentEntity
import org.springframework.stereotype.Repository

@Repository
class PaymentRepositoryImpl(
    private val paymentJpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(payment: Payment): Payment {
        val entity = paymentJpaRepository.save(PaymentEntity.from(payment))
        return entity.toPayment()
    }

    override fun saveAll(payments: List<Payment>): List<Payment> {
        val entities = paymentJpaRepository.saveAll(payments.map { PaymentEntity.from(it) })
        return entities.map { it.toPayment() }
    }

    override fun findAllByUserId(userId: Long): List<Payment> = paymentJpaRepository.findAllByUserId(userId).map { it.toPayment() }
}
