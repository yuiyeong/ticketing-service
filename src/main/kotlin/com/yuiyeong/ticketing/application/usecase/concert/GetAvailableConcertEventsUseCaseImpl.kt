package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.ConcertEventResult
import com.yuiyeong.ticketing.config.CacheNames
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class GetAvailableConcertEventsUseCaseImpl(
    private val concertService: ConcertService,
) : GetAvailableConcertEventsUseCase {
    @Cacheable(value = [CacheNames.AVAILABLE_CONCERT_EVENTS], key = "#concertId")
    override fun execute(concertId: Long): List<ConcertEventResult> {
        val concertEvents = concertService.getAvailableEvents(concertId)
        return concertEvents.map { ConcertEventResult.from(it) }
    }
}
