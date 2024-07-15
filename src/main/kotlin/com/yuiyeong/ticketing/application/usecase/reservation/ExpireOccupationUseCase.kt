package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult

interface ExpireOccupationUseCase {
    fun execute(): List<OccupationResult>
}
