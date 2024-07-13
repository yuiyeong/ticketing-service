package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.TicketingException
import com.yuiyeong.ticketing.domain.exception.WalletNotFoundException
import com.yuiyeong.ticketing.domain.model.Payment
import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.repository.PaymentRepository
import com.yuiyeong.ticketing.domain.repository.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.WalletRepository

class PaymentService(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
) {
    fun pay(
        userId: Long,
        reservation: Reservation,
    ) {
        val wallet = walletRepository.findOneByUserId(userId) ?: throw WalletNotFoundException()
        var transaction: Transaction? = null
        var failureReason: String? = null

        try {
            wallet.pay(reservation.totalAmount)
            walletRepository.save(wallet)

            val paymentTransaction = Transaction.createAsPayment(wallet, reservation.totalAmount)
            transaction = transactionRepository.save(paymentTransaction)
        } catch (e: TicketingException) {
            failureReason = e.notNullMessage
        }

        val payment = Payment.create(userId, reservation, transaction, failureReason)
        paymentRepository.save(payment)

        reservation.confirm()
        reservationRepository.save(reservation)
    }

    fun getHistory(userId: Long): List<Payment> = paymentRepository.findAllByUserId(userId)
}
