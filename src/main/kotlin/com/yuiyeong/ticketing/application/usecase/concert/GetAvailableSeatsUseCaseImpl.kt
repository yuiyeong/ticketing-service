package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.SeatResult
import com.yuiyeong.ticketing.domain.service.concert.SeatService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetAvailableSeatsUseCaseImpl(
    private val seatService: SeatService,
) : GetAvailableSeatsUseCase {
    @Transactional(readOnly = true)
    override fun execute(concertEventId: Long): List<SeatResult> {
        val seats = seatService.getAvailableSeats(concertEventId)
        return seats.map { SeatResult.from(it) }
    }
}
