package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventResult
import com.yuiyeong.ticketing.domain.service.ConcertEventService
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class GetAvailableConcertEventsUseCaseImpl(
    private val queueService: QueueService,
    private val concertEventService: ConcertEventService,
) : GetAvailableConcertEventsUseCase {
    override fun execute(
        userToken: String?,
        concertId: Long,
    ): List<ConcertEventResult> {
        queueService.verifyEntryOnProcessing(userToken)

        val concertEvents = concertEventService.getAvailableEvents(concertId)
        return concertEvents.map { ConcertEventResult.from(it) }
    }
}
