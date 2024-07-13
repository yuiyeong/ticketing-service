package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.OccupationResult
import com.yuiyeong.ticketing.domain.service.OccupationService
import org.springframework.stereotype.Component

@Component
class ExpirationOccupationUseCaseImpl(
    private val occupationService: OccupationService,
) : ExpirationOccupationUseCase {
    override fun expireOverdueOccupations(): List<OccupationResult> =
        occupationService.expireOverdueOccupations().map { OccupationResult.from(it) }
}
