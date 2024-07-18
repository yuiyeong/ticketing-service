package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult

interface OccupySeatUseCase {
    fun execute(
        userId: Long,
        concertEventId: Long,
        seatId: Long,
    ): OccupationResult
}
