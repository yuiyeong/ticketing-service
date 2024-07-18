package com.yuiyeong.ticketing.application.usecase.token

import com.yuiyeong.ticketing.application.dto.QueueEntryResult
import com.yuiyeong.ticketing.domain.service.QueueService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ValidateTokenUseCaseImpl(
    private val queueService: QueueService,
) : ValidateTokenUseCase {
    @Transactional(readOnly = true)
    override fun execute(token: String): QueueEntryResult {
        val positionOffset = queueService.getFirstWaitingPosition()
        return QueueEntryResult.from(queueService.getEntry(token), positionOffset)
    }
}
