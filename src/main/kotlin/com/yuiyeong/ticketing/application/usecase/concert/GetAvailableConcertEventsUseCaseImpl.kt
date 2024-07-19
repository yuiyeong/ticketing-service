package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.ConcertEventResult
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetAvailableConcertEventsUseCaseImpl(
    private val concertEventService: ConcertEventService,
) : GetAvailableConcertEventsUseCase {
    @Transactional(readOnly = true)
    override fun execute(concertId: Long): List<ConcertEventResult> {
        val concertEvents = concertEventService.getAvailableEvents(concertId)
        return concertEvents.map { ConcertEventResult.from(it) }
    }
}
