package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.domain.service.queue.QueueService
import org.springframework.stereotype.Component

@Component
class ActivateWaitingEntriesUseCaseImpl(
    private val queueService: QueueService,
) : ActivateWaitingEntriesUseCase {
    override fun execute(): Int = queueService.activateWaitingEntries()
}
