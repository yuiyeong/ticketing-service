package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.infrastructure.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {
    fun findAllByUserId(userId: Long): List<PaymentEntity>
}
