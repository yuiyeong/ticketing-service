package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.reservation.ReservationResult
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
import com.yuiyeong.ticketing.domain.service.reservation.ReservationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ReserveSeatUseCaseImpl(
    private val concertService: ConcertService,
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

        val concertEvent = concertService.getConcertEvent(concertEventId)
        return ReservationResult.from(concertEvent, reservation)
    }
}
