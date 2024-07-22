package com.yuiyeong.ticketing.infrastructure.repository.payment

import com.yuiyeong.ticketing.infrastructure.entity.payment.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {
    fun findAllByUserId(userId: Long): List<PaymentEntity>
}
