package com.yuiyeong.ticketing.infrastructure.jpa.repository.payment

import com.yuiyeong.ticketing.infrastructure.jpa.entity.payment.PaymentOutboxEntity
import com.yuiyeong.ticketing.infrastructure.jpa.entity.payment.PaymentOutboxEntityStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PaymentOutboxJpaRepository : JpaRepository<PaymentOutboxEntity, Long> {
    fun findByPaymentIdAndStatus(
        paymentId: Long,
        status: PaymentOutboxEntityStatus,
    ): PaymentOutboxEntity?

    @Query("SELECT po FROM PaymentOutboxEntity po WHERE po.status = :status AND po.publishedTimeMilli <= :momentTimeMilli")
    fun findByStatusAndPublishedTimeMilliBefore(
        @Param("status") status: PaymentOutboxEntityStatus,
        @Param("momentTimeMilli") momentTimeMilli: Long,
    ): List<PaymentOutboxEntity>
}
