package com.yuiyeong.ticketing.domain.service.payment

import com.yuiyeong.ticketing.domain.event.payment.PaymentEvent
import com.yuiyeong.ticketing.domain.event.payment.PaymentEventPublisher
import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.exception.TransactionNotFoundException
import com.yuiyeong.ticketing.domain.model.payment.Payment
import com.yuiyeong.ticketing.domain.repository.payment.PaymentRepository
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class PaymentService(
    private val reservationRepository: ReservationRepository,
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentEventPublisher: PaymentEventPublisher,
) {
    @Transactional
    fun create(
        userId: Long,
        reservationId: Long,
        amount: BigDecimal,
        transactionId: Long?,
        failureReason: String?,
    ): Payment {
        val reservation = reservationRepository.findOneById(reservationId) ?: throw ReservationNotFoundException()
        val transaction =
            transactionId?.let { id ->
                transactionRepository.findOneById(id) ?: throw TransactionNotFoundException()
            }

        paymentEventPublisher.publish(PaymentEvent(userId, reservationId, transaction, failureReason))
        val payment = paymentRepository.save(Payment.create(userId, reservation, transaction, failureReason))
        return payment
    }

    @Transactional(readOnly = true)
    fun getHistory(userId: Long): List<Payment> = paymentRepository.findAllByUserId(userId)
}
