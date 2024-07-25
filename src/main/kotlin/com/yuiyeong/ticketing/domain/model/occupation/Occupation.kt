package com.yuiyeong.ticketing.domain.model.occupation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyReleaseException
import com.yuiyeong.ticketing.domain.model.concert.Seat
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Occupation(
    val id: Long,
    val userId: Long,
    val concertEventId: Long,
    val allocations: List<SeatAllocation>,
    val status: OccupationStatus,
    val createdAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
    val expiredAt: ZonedDateTime?,
) {
    val totalSeats: Int by lazy { allocations.count() }

    val totalAmount: BigDecimal by lazy { allocations.sumOf { it.seatPrice } }

    fun expire(): Occupation {
        verifyActiveStatus()
        return copy(
            status = OccupationStatus.EXPIRED,
            expiredAt = ZonedDateTime.now().asUtc,
            allocations = allocations.map { it.markAsExpired() },
        )
    }

    fun release(moment: ZonedDateTime): Occupation {
        verifyActiveStatus()

        // state 가 EXPIRED 로, 변경되기 전에 release 를 호출했을 경우
        if (!moment.isBefore(expiresAt)) {
            throw OccupationAlreadyExpiredException()
        }

        return copy(status = OccupationStatus.RELEASED, allocations = allocations.map { it.markAsReserved() })
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
        fun create(
            userId: Long,
            concertEventId: Long,
            seats: List<Seat>,
            expirationMinutes: Long,
        ): Occupation {
            val createdAt = ZonedDateTime.now().asUtc
            val allocations = seats.map { SeatAllocation.createOccupiedOne(userId, it, createdAt) }
            return Occupation(
                id = 0L,
                userId = userId,
                concertEventId = concertEventId,
                allocations = allocations,
                status = OccupationStatus.ACTIVE,
                createdAt = createdAt,
                expiresAt = createdAt.plusMinutes(expirationMinutes),
                expiredAt = null,
            )
        }
    }
}

enum class OccupationStatus {
    ACTIVE,
    EXPIRED,
    RELEASED,
}
