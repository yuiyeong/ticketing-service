package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.SeatResult
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import org.springframework.stereotype.Component

@Component
class GetAvailableSeatsUseCaseImpl(
    private val concertService: ConcertService,
) : GetAvailableSeatsUseCase {
    override fun execute(concertEventId: Long): List<SeatResult> {
        val seats = concertService.getAvailableSeats(concertEventId)
        return seats.map { SeatResult.from(it) }
    }
}
