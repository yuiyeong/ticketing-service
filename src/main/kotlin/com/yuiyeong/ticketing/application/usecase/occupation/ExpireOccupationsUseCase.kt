package com.yuiyeong.ticketing.application.usecase.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult

interface ExpireOccupationsUseCase {
    fun execute(): List<OccupationResult>
}
