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
        // 1. reservation 가져오기 및 검증
        val reservation = reservationService.getReservation(reservationId)
        reservation.verifyStatusIsNotConfirmed()

        // 2. 결제 시도
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

        // 3. 결제 결과로 내역 만들기
        val payment = paymentService.create(userId, reservation.id, transaction?.id, failureReason)

        if (payment.isFailed) return PaymentResult.from(payment)

        // 4. reservation 의 상태를 완료로 변경
        reservationService.confirm(reservation.id)

        // 5. queue 에서 entry 제거
        queueService.exit(token)

        return PaymentResult.from(payment)
    }
}
