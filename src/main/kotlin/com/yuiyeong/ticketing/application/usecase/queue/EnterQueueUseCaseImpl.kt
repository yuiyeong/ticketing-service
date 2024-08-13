package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.queue.WaitingInfoResult
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.config.QueueProperties
import com.yuiyeong.ticketing.domain.service.queue.QueueService
import com.yuiyeong.ticketing.domain.service.queue.TokenService
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class EnterQueueUseCaseImpl(
    private val queueService: QueueService,
    private val tokenService: TokenService,
    private val queueProperties: QueueProperties,
) : EnterQueueUseCase {
    override fun execute(userId: Long): WaitingInfoResult {
        val enteredAt = ZonedDateTime.now().asUtc
        val expiresAt = enteredAt.plusSeconds(queueProperties.tokenTtlInSeconds)
        val token = tokenService.generateToken(userId, enteredAt, expiresAt)
        return WaitingInfoResult.from(queueService.enter(token))
    }
}
