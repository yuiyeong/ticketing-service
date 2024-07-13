package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ExitingQueueUseCaseImpl(
    private val queueService: QueueService,
) : ExitingQueueUseCase {
    override fun exit(token: String): WaitingEntryResult = WaitingEntryResult.from(queueService.exit(token))
}
