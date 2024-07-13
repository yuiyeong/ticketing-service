package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.ReservationResult

interface ReserveSeatUseCase {
    fun execute(
        userToken: String?,
        concertEventId: Long,
        occupiedSeatId: Long,
    ): ReservationResult
}
