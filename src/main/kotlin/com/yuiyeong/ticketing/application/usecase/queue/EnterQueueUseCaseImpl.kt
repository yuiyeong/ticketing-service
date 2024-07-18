package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.QueueEntryResult
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.service.QueueService
import com.yuiyeong.ticketing.domain.service.TokenService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Component
class EnterQueueUseCaseImpl(
    private val queueService: QueueService,
    private val tokenService: TokenService,
) : EnterQueueUseCase {
    @Transactional
    override fun execute(userId: Long): QueueEntryResult {
        // 기존에 발급 받은 token 이 있다면, 대기열에 제거
        queueService.dequeueExistingEntries(userId)

        val enteredAt = ZonedDateTime.now().asUtc
        val expiresAt = enteredAt.plusMinutes(EXPIRATION_MINUTES)
        val token = tokenService.generateToken(userId, enteredAt, expiresAt)
        val entry = queueService.enter(userId, token, enteredAt, expiresAt)
        val positionOffset = queueService.getFirstWaitingPosition()
        return QueueEntryResult.from(entry, positionOffset)
    }

    companion object {
        const val EXPIRATION_MINUTES = 60L // 60분 뒤 만료
    }
}
