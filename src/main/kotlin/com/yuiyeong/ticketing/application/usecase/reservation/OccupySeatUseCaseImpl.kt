package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.OccupationService
import com.yuiyeong.ticketing.domain.service.QueueService
import com.yuiyeong.ticketing.domain.service.SeatService
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class OccupySeatUseCaseImpl(
    private val queueService: QueueService,
    private val concertEventService: ConcertEventService,
    private val occupationService: OccupationService,
    private val seatService: SeatService,
) : OccupySeatUseCase {
    override fun execute(
        userToken: String?,
        concertEventId: Long,
        seatId: Long,
    ): OccupationResult {
        val entry = queueService.verifyEntryOnProcessing(userToken)

        val now = ZonedDateTime.now()
        val concertEvent = concertEventService.getConcertEvent(concertEventId)
        concertEvent.verifyWithinReservationPeriod(now)

        val seatIds = listOf(seatId)
        seatService.occupy(seatIds)

        val occupation = occupationService.createOccupation(entry.userId, seatIds)
        return OccupationResult.from(occupation)
    }
}
