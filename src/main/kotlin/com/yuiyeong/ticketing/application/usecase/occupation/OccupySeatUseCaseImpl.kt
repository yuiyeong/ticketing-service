package com.yuiyeong.ticketing.application.usecase.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.service.concert.ConcertEventService
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
import com.yuiyeong.ticketing.domain.service.concert.SeatService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Component
class OccupySeatUseCaseImpl(
    private val concertEventService: ConcertEventService,
    private val occupationService: OccupationService,
    private val seatService: SeatService,
) : OccupySeatUseCase {
    @Transactional
    override fun execute(
        userId: Long,
        concertEventId: Long,
        seatId: Long,
    ): OccupationResult {
        val now = ZonedDateTime.now().asUtc
        val concertEvent = concertEventService.getConcertEvent(concertEventId)
        concertEvent.verifyWithinReservationPeriod(now)

        val seatIds = listOf(seatId)
        seatService.occupy(seatIds)

        val occupation = occupationService.createOccupation(userId, concertEventId, seatIds)
        concertEventService.refreshAvailableSeats(concertEventId)
        return OccupationResult.from(occupation)
    }
}
