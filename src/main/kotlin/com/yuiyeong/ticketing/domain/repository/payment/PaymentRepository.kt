package com.yuiyeong.ticketing.domain.repository.payment

import com.yuiyeong.ticketing.domain.model.payment.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment

    fun saveAll(payments: List<Payment>): List<Payment>

    fun findAllByUserId(userId: Long): List<Payment>

    fun deleteAll()
}
