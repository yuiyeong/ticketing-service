package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class EnteringQueueUseCaseImpl(
    private val queueService: QueueService,
) : EnteringQueueUseCase {
    override fun enter(userId: Long): WaitingEntryDto {
        val positionOffset = queueService.getFirstWaitingPosition()
        return WaitingEntryDto.from(queueService.enter(userId), positionOffset)
    }
}
