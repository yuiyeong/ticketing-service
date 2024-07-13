package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.InvalidOccupationException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyReleaseException
import com.yuiyeong.ticketing.domain.exception.OccupationExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationNotOverdueException
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
        if (status == OccupationStatus.EXPIRED) {
            throw OccupationExpiredException()
        }

        if (status == OccupationStatus.RELEASED) {
            throw InvalidOccupationException()
        }

        if (current.isBefore(expiresAt)) {
            throw OccupationNotOverdueException()
        }

        status = OccupationStatus.EXPIRED
    }

    fun checkAvailable() {
        if (status == OccupationStatus.EXPIRED) {
            throw OccupationExpiredException()
        }

        if (status == OccupationStatus.RELEASED) {
            throw OccupationAlreadyReleaseException()
        }
    }

    fun release() {
        if (status == OccupationStatus.EXPIRED) {
            throw OccupationExpiredException()
        }

        if (status == OccupationStatus.RELEASED) {
            throw OccupationAlreadyReleaseException()
        }

        status = OccupationStatus.RELEASED
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
