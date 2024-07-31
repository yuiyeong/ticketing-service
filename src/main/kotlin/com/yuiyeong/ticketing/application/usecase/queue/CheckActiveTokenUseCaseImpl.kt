package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.domain.service.queue.QueueService
import org.springframework.stereotype.Component

@Component
class CheckActiveTokenUseCaseImpl(
    private val queueService: QueueService,
) : CheckActiveTokenUseCase {
    override fun execute(token: String) = queueService.verifyTokenIsActive(token)
}
