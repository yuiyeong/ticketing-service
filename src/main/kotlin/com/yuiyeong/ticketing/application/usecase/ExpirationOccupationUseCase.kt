package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.OccupationDto

interface ExpirationOccupationUseCase {
    fun expireOverdueOccupations(): List<OccupationDto>
}
