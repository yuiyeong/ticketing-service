package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class QueueEntryInfoUseCaseImpl(
    private val queueService: QueueService,
) : QueueEntryInfoUseCase {
    override fun getEntry(token: String?): WaitingEntryDto {
        val positionOffset = queueService.getFirstWaitingPosition()
        return WaitingEntryDto.from(queueService.getWaitingEntry(token), positionOffset)
    }
}
