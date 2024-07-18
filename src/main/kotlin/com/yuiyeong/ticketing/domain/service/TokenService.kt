package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.model.QueueEntry
import java.time.ZonedDateTime

interface TokenService {
    fun generateToken(
        userId: Long,
        issuedAt: ZonedDateTime,
        expiresAt: ZonedDateTime,
    ): String

    fun validateToken(token: String): QueueEntry
}
