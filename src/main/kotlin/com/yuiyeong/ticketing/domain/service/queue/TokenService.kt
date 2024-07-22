package com.yuiyeong.ticketing.domain.service.queue

import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import java.time.ZonedDateTime

interface TokenService {
    fun generateToken(
        userId: Long,
        issuedAt: ZonedDateTime,
        expiresAt: ZonedDateTime,
    ): String

    fun validateToken(token: String): QueueEntry
}
