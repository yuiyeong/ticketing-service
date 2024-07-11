package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventDto
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import org.springframework.stereotype.Component

@Component
class AvailableEventsUseCaseImpl(
    private val concertEventService: ConcertEventService,
) : AvailableEventsUseCase {
    override fun getConcertEvents(concertId: Long): List<ConcertEventDto> {
        val concertEvents = concertEventService.getAvailableEvents(concertId)
        return concertEvents.map { ConcertEventDto.from(it) }
    }
}
