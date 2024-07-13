package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component

@Component
class ExitingQueueUseCaseImpl(
    private val queueService: QueueService,
) : ExitingQueueUseCase {
    override fun exit(token: String): WaitingEntryDto = WaitingEntryDto.from(queueService.exit(token))
}
