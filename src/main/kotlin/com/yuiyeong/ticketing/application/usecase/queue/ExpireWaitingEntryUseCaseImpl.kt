package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ExpireWaitingEntryUseCaseImpl(
    private val queueService: QueueService,
) : ExpireWaitingEntryUseCase {
    override fun execute(): List<WaitingEntryResult> = queueService.expireOverdueEntries().map { WaitingEntryResult.from(it) }
}
