package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventSeatDto
import com.yuiyeong.ticketing.domain.service.ConcertEventService

class AvailableSeatsUseCaseImpl(
    private val concertEventService: ConcertEventService,
) : AvailableSeatsUseCase {
    override fun getSeats(concertEventId: Long): List<ConcertEventSeatDto> {
        val seats = concertEventService.getAvailableSeats(concertEventId)
        return seats.map { ConcertEventSeatDto.from(it) }
    }
}
