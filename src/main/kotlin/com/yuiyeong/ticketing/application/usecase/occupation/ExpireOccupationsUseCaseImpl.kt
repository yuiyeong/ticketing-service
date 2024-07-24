package com.yuiyeong.ticketing.application.usecase.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
import org.springframework.stereotype.Component

@Component
class ExpireOccupationsUseCaseImpl(
    private val occupationService: OccupationService,
    private val concertService: ConcertService,
) : ExpireOccupationsUseCase {
    override fun execute(): List<OccupationResult> {
        val occupations = occupationService.expireOverdueOccupations()
        val concertEventIds = occupations.map { it.concertEventId }.distinct()
        concertEventIds.forEach { concertService.refreshAvailableSeats(it) }
        return occupations.map { OccupationResult.from(it) }
    }
}
