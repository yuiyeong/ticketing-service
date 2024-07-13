package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ActivationEntryUseCaseImpl(
    private val queueService: QueueService,
) : ActivationEntryUseCase {
    override fun activateEntries(): List<WaitingEntryResult> = queueService.activateWaitingEntries().map { WaitingEntryResult.from(it) }
}
