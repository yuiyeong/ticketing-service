package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.reservation.ReservationResult

interface ReserveSeatUseCase {
    fun execute(
        userId: Long,
        concertEventId: Long,
        occupationId: Long,
    ): ReservationResult
}
