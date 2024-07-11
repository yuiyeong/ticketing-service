package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventSeatDto
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class AvailableSeatsUseCaseImpl(
    private val queueService: QueueService,
    private val concertEventService: ConcertEventService,
) : AvailableSeatsUseCase {
    override fun getSeats(
        userToken: String?,
        concertEventId: Long,
    ): List<ConcertEventSeatDto> {
        queueService.verifyEntryOnProcessing(userToken)

        val seats = concertEventService.getAvailableSeats(concertEventId)
        return seats.map { ConcertEventSeatDto.from(it) }
    }
}
