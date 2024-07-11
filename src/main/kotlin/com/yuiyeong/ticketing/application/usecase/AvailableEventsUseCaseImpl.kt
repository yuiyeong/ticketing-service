package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventDto
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class AvailableEventsUseCaseImpl(
    private val queueService: QueueService,
    private val concertEventService: ConcertEventService,
) : AvailableEventsUseCase {
    override fun getConcertEvents(
        userToken: String?,
        concertId: Long,
    ): List<ConcertEventDto> {
        queueService.verifyEntryOnProcessing(userToken)

        val concertEvents = concertEventService.getAvailableEvents(concertId)
        return concertEvents.map { ConcertEventDto.from(it) }
    }
}
