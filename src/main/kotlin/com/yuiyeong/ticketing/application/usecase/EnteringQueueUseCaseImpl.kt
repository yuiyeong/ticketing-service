package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class EnteringQueueUseCaseImpl(
    private val queueService: QueueService,
) : EnteringQueueUseCase {
    override fun enter(userId: Long): WaitingEntryResult {
        val positionOffset = queueService.getFirstWaitingPosition()
        return WaitingEntryResult.from(queueService.enter(userId), positionOffset)
    }
}
