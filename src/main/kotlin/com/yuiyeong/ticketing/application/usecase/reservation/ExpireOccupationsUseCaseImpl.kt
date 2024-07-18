package com.yuiyeong.ticketing.application.usecase.reservation

import com.yuiyeong.ticketing.application.dto.OccupationResult
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.OccupationService
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
