package com.yuiyeong.ticketing.domain.model

import java.time.ZonedDateTime

data class Occupation(
    val id: Long,
    val userId: Long,
    val seat: Seat,
    val status: OccupationStatus,
    val createdAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
) {
    companion object {
        private const val EXPIRATION_MINUTES = 5L // 5 분 뒤 만료

        fun create(
            userId: Long,
            seat: Seat,
        ): Occupation {
            seat.makeUnavailable()

            val now = ZonedDateTime.now()
            return Occupation(
                0L,
                userId,
                seat,
                OccupationStatus.ACTIVE,
                now,
                now.plusMinutes(EXPIRATION_MINUTES),
            )
        }
    }
}

enum class OccupationStatus {
    ACTIVE,
    EXPIRED,
    RELEASED,
}
