package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class EnterWaitingQueueUseCaseImpl(
    private val queueService: QueueService,
) : EnterWaitingQueueUseCase {
    override fun execute(userId: Long): WaitingEntryResult {
        val positionOffset = queueService.getFirstWaitingPosition()
        return WaitingEntryResult.from(queueService.enter(userId), positionOffset)
    }
}
