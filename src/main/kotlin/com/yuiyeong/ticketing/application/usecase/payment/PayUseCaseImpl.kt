package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.PaymentResult
import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.service.PaymentService
import com.yuiyeong.ticketing.domain.service.ReservationService
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.stereotype.Component

@Component
class PayUseCaseImpl(
    private val reservationService: ReservationService,
    private val walletService: WalletService,
    private val paymentService: PaymentService,
) : PayUseCase {
    override fun execute(
        userId: Long,
        reservationId: Long,
    ): PaymentResult {
        val reservation = reservationService.getReservation(reservationId)

        var transaction: Transaction? = null
        var failureReason: String? = null

        runCatching {
            walletService.pay(userId, reservation.totalAmount)
        }.onSuccess {
            transaction = it
        }.onFailure { exception ->
            failureReason = exception.message
        }

        val payment = paymentService.create(userId, reservation.id, transaction?.id, failureReason)

        reservationService.confirm(reservation.id)
        return PaymentResult.from(payment)
    }
}
