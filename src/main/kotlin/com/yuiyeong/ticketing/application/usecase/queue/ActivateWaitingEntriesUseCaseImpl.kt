package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.QueueEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ActivateWaitingEntriesUseCaseImpl(
    private val queueService: QueueService,
) : ActivateWaitingEntriesUseCase {
    @Transactional
    override fun execute(): List<QueueEntryResult> = queueService.activateWaitingEntries().map { QueueEntryResult.from(it) }
}
