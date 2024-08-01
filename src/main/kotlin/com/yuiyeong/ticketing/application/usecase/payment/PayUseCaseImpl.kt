package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.payment.PaymentResult
import com.yuiyeong.ticketing.domain.exception.TicketingException
import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import com.yuiyeong.ticketing.domain.service.payment.PaymentService
import com.yuiyeong.ticketing.domain.service.queue.QueueService
import com.yuiyeong.ticketing.domain.service.reservation.ReservationService
import com.yuiyeong.ticketing.domain.service.wallet.WalletService
import org.springframework.stereotype.Component

@Component
class PayUseCaseImpl(
    private val reservationService: ReservationService,
    private val walletService: WalletService,
    private val paymentService: PaymentService,
    private val queueService: QueueService,
) : PayUseCase {
    override fun execute(
        userId: Long,
        token: String,
        reservationId: Long,
    ): PaymentResult {
        val reservation = reservationService.getReservation(reservationId)

        // 결제 시도
        var transaction: Transaction? = null
        var failureReason: String? = null
        runCatching {
            walletService.pay(userId, reservation.totalAmount)
        }.onSuccess {
            transaction = it
        }.onFailure { exception ->
            failureReason =
                if (exception is TicketingException) {
                    exception.errorCode.message
                } else {
                    exception.localizedMessage
                }
        }

        // 결제 결과로 내역 만들기
        val payment = paymentService.create(userId, reservation.id, transaction?.id, failureReason)

        // reservation 의 상태를 완료로 변경
        reservationService.confirm(reservation.id)

        // queue 에서 entry 제거
        queueService.exit(token)

        return PaymentResult.from(payment)
    }
}
