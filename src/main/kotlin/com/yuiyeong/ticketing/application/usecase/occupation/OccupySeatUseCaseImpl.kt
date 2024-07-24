package com.yuiyeong.ticketing.application.usecase.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class OccupySeatUseCaseImpl(
    private val concertService: ConcertService,
    private val occupationService: OccupationService,
) : OccupySeatUseCase {
    override fun execute(
        userId: Long,
        concertEventId: Long,
        seatId: Long,
    ): OccupationResult {
        val now = ZonedDateTime.now().asUtc
        val concertEvent = concertService.getConcertEvent(concertEventId)
        concertEvent.verifyWithinReservationPeriod(now)

        val seatIds = listOf(seatId)
        val occupation = occupationService.occupy(userId, concertEventId, seatIds)
        concertService.refreshAvailableSeats(concertEventId)
        return OccupationResult.from(occupation)
    }
}
