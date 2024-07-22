package com.yuiyeong.ticketing.application.usecase.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult
import com.yuiyeong.ticketing.domain.service.concert.ConcertEventService
import com.yuiyeong.ticketing.domain.service.occupation.OccupationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ExpireOccupationsUseCaseImpl(
    private val occupationService: OccupationService,
    private val concertEventService: ConcertEventService,
) : ExpireOccupationsUseCase {
    @Transactional
    override fun execute(): List<OccupationResult> {
        val occupations = occupationService.expireOverdueOccupations()
        val concertEventIds = occupations.map { it.concertEventId }.distinct()
        concertEventIds.forEach { concertEventService.refreshAvailableSeats(it) }
        return occupations.map { OccupationResult.from(it) }
    }
}
