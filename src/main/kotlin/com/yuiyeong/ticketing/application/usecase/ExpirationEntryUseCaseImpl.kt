package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ExpirationEntryUseCaseImpl(
    private val queueService: QueueService,
) : ExpirationEntryUseCase {
    override fun expireOverdueEntries(): List<WaitingEntryDto> = queueService.expireOverdueEntries().map { WaitingEntryDto.from(it) }
}
