package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.QueueEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ExpireWaitingEntryUseCaseImpl(
    private val queueService: QueueService,
) : ExpireWaitingEntryUseCase {
    @Transactional
    override fun execute(): List<QueueEntryResult> = queueService.expireOverdueEntries().map { QueueEntryResult.from(it) }
}
