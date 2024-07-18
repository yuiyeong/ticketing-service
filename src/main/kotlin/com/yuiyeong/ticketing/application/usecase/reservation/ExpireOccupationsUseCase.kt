package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult

interface ExpireOccupationsUseCase {
    fun execute(): List<OccupationResult>
}
