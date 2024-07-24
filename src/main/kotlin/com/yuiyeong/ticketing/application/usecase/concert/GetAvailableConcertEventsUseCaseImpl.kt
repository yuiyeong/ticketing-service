package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.ConcertEventResult
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import org.springframework.stereotype.Component

@Component
class GetAvailableConcertEventsUseCaseImpl(
    private val concertService: ConcertService,
) : GetAvailableConcertEventsUseCase {
    override fun execute(concertId: Long): List<ConcertEventResult> {
        val concertEvents = concertService.getAvailableEvents(concertId)
        return concertEvents.map { ConcertEventResult.from(it) }
    }
}
