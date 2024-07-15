package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.SeatResult
import com.yuiyeong.ticketing.domain.service.QueueService
import com.yuiyeong.ticketing.domain.service.SeatService
import org.springframework.stereotype.Component

@Component
class GetAvailableSeatsUseCaseImpl(
    private val queueService: QueueService,
    private val seatService: SeatService,
) : GetAvailableSeatsUseCase {
    override fun execute(
        userToken: String?,
        concertEventId: Long,
    ): List<SeatResult> {
        queueService.verifyEntryOnProcessing(userToken)

        val seats = seatService.getAvailableSeats(concertEventId)
        return seats.map { SeatResult.from(it) }
    }
}
