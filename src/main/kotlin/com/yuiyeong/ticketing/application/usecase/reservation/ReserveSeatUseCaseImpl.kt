package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.ReservationResult
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.OccupationService
import com.yuiyeong.ticketing.domain.service.PaymentService
import com.yuiyeong.ticketing.domain.service.QueueService
import com.yuiyeong.ticketing.domain.service.ReservationService
import org.springframework.stereotype.Component

@Component
class ReserveSeatUseCaseImpl(
    private val queueService: QueueService,
    private val concertEventService: ConcertEventService,
    private val reservationService: ReservationService,
    private val occupationService: OccupationService,
    private val paymentService: PaymentService,
) : ReserveSeatUseCase {
    override fun execute(
        userToken: String?,
        concertEventId: Long,
        occupiedSeatId: Long,
    ): ReservationResult {
        val entry = queueService.verifyEntryOnProcessing(userToken)
        val concertEvent = concertEventService.getConcertEvent(concertEventId)
        val occupation = occupationService.getUserOccupation(entry.userId, occupiedSeatId)

        val reservation = reservationService.reserve(entry.userId, concertEvent, occupation)
        paymentService.pay(entry.userId, reservation)
        return ReservationResult.from(concertEvent, reservation)
    }
}
