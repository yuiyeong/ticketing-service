package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.OccupationDto

interface OccupationUseCase {
    fun occupySeat(
        userToken: String?,
        concertEventId: Long,
        seatId: Long,
    ): OccupationDto
}
