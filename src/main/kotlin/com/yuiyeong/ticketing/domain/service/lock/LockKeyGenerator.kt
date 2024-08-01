package com.yuiyeong.ticketing.domain.service.lock

import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import org.springframework.stereotype.Service

@Service
class LockKeyGenerator {
    fun generateConcertEventSeatKey(
        concertEvent: ConcertEvent,
        seatId: Long,
    ): String = "concert-event:${concertEvent.id}:seat:$seatId"

    fun generateUserWalletKey(userId: Long): String = "user:$userId:wallet"
}
