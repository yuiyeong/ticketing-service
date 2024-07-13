package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ActivationEntryUseCaseImpl(
    private val queueService: QueueService,
) : ActivationEntryUseCase {
    override fun activateEntries(): List<WaitingEntryDto> = queueService.activateWaitingEntries().map { WaitingEntryDto.from(it) }
}
