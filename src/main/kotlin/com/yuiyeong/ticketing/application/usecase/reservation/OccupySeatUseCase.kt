package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult

interface OccupySeatUseCase {
    fun execute(
        userToken: String?,
        concertEventId: Long,
        seatId: Long,
    ): OccupationResult
}
