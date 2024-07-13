package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.OccupationResult

interface ExpirationOccupationUseCase {
    fun expireOverdueOccupations(): List<OccupationResult>
}
