package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult
import com.yuiyeong.ticketing.domain.service.OccupationService
import org.springframework.stereotype.Component

@Component
class ExpireOccupationUseCaseImpl(
    private val occupationService: OccupationService,
) : ExpireOccupationUseCase {
    override fun execute(): List<OccupationResult> = occupationService.expireOverdueOccupations().map { OccupationResult.from(it) }
}
