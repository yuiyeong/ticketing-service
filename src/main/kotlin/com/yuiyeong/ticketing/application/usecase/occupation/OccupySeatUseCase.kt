package com.yuiyeong.ticketing.application.usecase.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult

interface OccupySeatUseCase {
    fun execute(
        userId: Long,
        concertEventId: Long,
        seatId: Long,
    ): OccupationResult
}
