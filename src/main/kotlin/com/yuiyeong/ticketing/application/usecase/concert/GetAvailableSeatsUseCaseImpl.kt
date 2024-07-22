package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.SeatResult
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetAvailableSeatsUseCaseImpl(
    private val concertService: ConcertService,
) : GetAvailableSeatsUseCase {
    @Transactional(readOnly = true)
    override fun execute(concertEventId: Long): List<SeatResult> {
        val seats = concertService.getAvailableSeats(concertEventId)
        return seats.map { SeatResult.from(it) }
    }
}
