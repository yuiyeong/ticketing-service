package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.OccupationDto
import com.yuiyeong.ticketing.domain.service.OccupationService
import org.springframework.stereotype.Component

@Component
class ExpirationOccupationUseCaseImpl(
    private val occupationService: OccupationService,
) : ExpirationOccupationUseCase {
    override fun expireOverdueOccupations(): List<OccupationDto> =
        occupationService.expireOverdueOccupations().map { OccupationDto.from(it) }
}
