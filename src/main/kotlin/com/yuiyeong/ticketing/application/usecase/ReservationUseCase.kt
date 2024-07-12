package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ReservationDto

interface ReservationUseCase {
    fun reserve(
        userToken: String?,
        concertEventId: Long,
        occupiedSeatId: Long,
    ): ReservationDto
}
