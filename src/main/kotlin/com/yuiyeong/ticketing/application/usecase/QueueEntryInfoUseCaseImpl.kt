package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class QueueEntryInfoUseCaseImpl(
    private val queueService: QueueService,
) : QueueEntryInfoUseCase {
    override fun getEntry(token: String?): WaitingEntryResult {
        val positionOffset = queueService.getFirstWaitingPosition()
        return WaitingEntryResult.from(queueService.getWaitingEntry(token), positionOffset)
    }
}
