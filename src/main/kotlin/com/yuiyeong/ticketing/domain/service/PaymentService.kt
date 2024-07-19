package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.exception.TransactionNotFoundException
import com.yuiyeong.ticketing.domain.model.Payment
import com.yuiyeong.ticketing.domain.repository.PaymentRepository
import com.yuiyeong.ticketing.domain.repository.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val reservationRepository: ReservationRepository,
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository,
) {
    fun create(
        userId: Long,
        reservationId: Long,
        transactionId: Long?,
        failureReason: String?,
    ): Payment {
        val reservation = reservationRepository.findOneById(reservationId) ?: throw ReservationNotFoundException()
        val transaction =
            transactionId?.let { id ->
                transactionRepository.findOneById(id) ?: throw TransactionNotFoundException()
            }

        val payment = Payment.create(userId, reservation, transaction, failureReason)
        return paymentRepository.save(payment)
    }

    fun getHistory(userId: Long): List<Payment> = paymentRepository.findAllByUserId(userId)
}
