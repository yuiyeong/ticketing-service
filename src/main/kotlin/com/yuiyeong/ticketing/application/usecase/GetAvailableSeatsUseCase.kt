package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.SeatResult

interface GetAvailableSeatsUseCase {
    fun execute(
        userToken: String?,
        concertEventId: Long,
    ): List<SeatResult>
}
