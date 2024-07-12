package com.yuiyeong.ticketing.domain.model

import java.time.ZonedDateTime

data class Occupation(
    val id: Long,
    val userId: Long,
    val seat: Seat,
    var status: OccupationStatus,
    val createdAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
) {
    fun expire(current: ZonedDateTime) {
        if (status != OccupationStatus.ACTIVE) {
            throw IllegalStateException("점유 상타에 대해서만 만료할 수 있습니다.")
        }

        if (current.isBefore(expiresAt)) {
            throw IllegalStateException("현재 만료 일시가 지나지 않았습니다.")
        }

        status = OccupationStatus.EXPIRED
    }

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
