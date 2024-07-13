package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ExitWaitingQueueUseCaseImpl(
    private val queueService: QueueService,
) : ExitWaitingQueueUseCase {
    override fun execute(token: String): WaitingEntryResult = WaitingEntryResult.from(queueService.exit(token))
}
