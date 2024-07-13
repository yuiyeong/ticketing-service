package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.SeatResult
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class GetAvailableSeatsUseCaseImpl(
    private val queueService: QueueService,
    private val concertEventService: ConcertEventService,
) : GetAvailableSeatsUseCase {
    override fun execute(
        userToken: String?,
        concertEventId: Long,
    ): List<SeatResult> {
        queueService.verifyEntryOnProcessing(userToken)

        val seats = concertEventService.getAvailableSeats(concertEventId)
        return seats.map { SeatResult.from(it) }
    }
}
