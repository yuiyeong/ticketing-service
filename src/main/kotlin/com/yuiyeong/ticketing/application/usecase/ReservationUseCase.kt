package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ReservationResult

interface ReservationUseCase {
    fun reserve(
        userToken: String?,
        concertEventId: Long,
        occupiedSeatId: Long,
    ): ReservationResult
}
