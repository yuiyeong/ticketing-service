package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ExpirationEntryUseCaseImpl(
    private val queueService: QueueService,
) : ExpirationEntryUseCase {
    override fun expireOverdueEntries(): List<WaitingEntryResult> = queueService.expireOverdueEntries().map { WaitingEntryResult.from(it) }
}
