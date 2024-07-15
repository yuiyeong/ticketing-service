package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyReleaseException
import java.time.ZonedDateTime

data class Occupation(
    val id: Long,
    val userId: Long,
    val seatIds: List<Long>,
    var status: OccupationStatus,
    val createdAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
    var expiredAt: ZonedDateTime? = null,
) {
    fun expire() {
        verifyActiveStatus()

        status = OccupationStatus.EXPIRED
        expiredAt = ZonedDateTime.now()
    }

    fun release(moment: ZonedDateTime) {
        verifyActiveStatus()

        // state 가 EXPIRED 로, 변경되기 전에 release 를 호출했을 경우
        if (!moment.isBefore(expiresAt)) {
            throw OccupationAlreadyExpiredException()
        }

        status = OccupationStatus.RELEASED
    }

    private fun verifyActiveStatus() {
        if (status == OccupationStatus.EXPIRED) {
            throw OccupationAlreadyExpiredException()
        }

        if (status == OccupationStatus.RELEASED) {
            throw OccupationAlreadyReleaseException()
        }
    }

    companion object {
        const val EXPIRATION_MINUTES = 5L // 5 분 뒤 만료

        fun create(
            userId: Long,
            seatIds: List<Long>,
        ): Occupation {
            val now = ZonedDateTime.now()
            return Occupation(
                id = 0L,
                userId = userId,
                seatIds = seatIds,
                status = OccupationStatus.ACTIVE,
                createdAt = now,
                expiresAt = now.plusMinutes(EXPIRATION_MINUTES),
            )
        }
    }
}

enum class OccupationStatus {
    ACTIVE,
    EXPIRED,
    RELEASED,
}
