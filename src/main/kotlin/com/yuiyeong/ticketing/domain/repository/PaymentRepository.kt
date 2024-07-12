package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment

    fun findAllByUserId(userId: Long): List<Payment>
}
