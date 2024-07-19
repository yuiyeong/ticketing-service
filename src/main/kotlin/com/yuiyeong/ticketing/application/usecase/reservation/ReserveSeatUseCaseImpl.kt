package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.ReservationResult
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.OccupationService
import com.yuiyeong.ticketing.domain.service.ReservationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ReserveSeatUseCaseImpl(
    private val concertEventService: ConcertEventService,
    private val reservationService: ReservationService,
    private val occupationService: OccupationService,
) : ReserveSeatUseCase {
    @Transactional
    override fun execute(
        userId: Long,
        concertEventId: Long,
        occupationId: Long,
    ): ReservationResult {
        val reservation = reservationService.reserve(userId, concertEventId, occupationId)

        occupationService.release(userId, occupationId)

        val concertEvent = concertEventService.getConcertEvent(concertEventId)
        return ReservationResult.from(concertEvent, reservation)
    }
}
