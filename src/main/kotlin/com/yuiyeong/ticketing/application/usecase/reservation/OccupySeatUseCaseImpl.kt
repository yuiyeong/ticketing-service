package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.OccupationService
import com.yuiyeong.ticketing.domain.service.SeatService
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
