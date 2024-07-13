package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.SeatResult

interface AvailableSeatsUseCase {
    fun getSeats(
        userToken: String?,
        concertEventId: Long,
    ): List<SeatResult>
}
