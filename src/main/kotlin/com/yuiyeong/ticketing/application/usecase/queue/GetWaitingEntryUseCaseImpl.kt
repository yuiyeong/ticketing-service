package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class GetWaitingEntryUseCaseImpl(
    private val queueService: QueueService,
) : GetWaitingEntryUseCase {
    override fun execute(token: String?): WaitingEntryResult {
        val positionOffset = queueService.getFirstWaitingPosition()
        return WaitingEntryResult.from(queueService.getWaitingEntry(token), positionOffset)
    }
}
