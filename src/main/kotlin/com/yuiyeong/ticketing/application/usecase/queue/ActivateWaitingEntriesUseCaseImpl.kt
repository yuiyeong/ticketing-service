package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ActivateWaitingEntriesUseCaseImpl(
    private val queueService: QueueService,
) : ActivateWaitingEntriesUseCase {
    override fun execute(): List<WaitingEntryResult> = queueService.activateWaitingEntries().map { WaitingEntryResult.from(it) }
}
