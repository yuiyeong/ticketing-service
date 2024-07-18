package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.OccupationAlreadyReleaseException
import java.time.ZonedDateTime

class Occupation(
    val id: Long,
    val userId: Long,
    val concertEventId: Long,
    val allocations: List<SeatAllocation>,
    status: OccupationStatus,
    val createdAt: ZonedDateTime,
    val expiresAt: ZonedDateTime,
    expiredAt: ZonedDateTime?,
) {
    var status: OccupationStatus = status
        private set

    var expiredAt: ZonedDateTime? = expiredAt
        private set

    fun expire() {
        verifyActiveStatus()

        status = OccupationStatus.EXPIRED
        expiredAt = ZonedDateTime.now().asUtc
        allocations.forEach { it.markAsExpired() }
    }

    fun release(moment: ZonedDateTime) {
        verifyActiveStatus()

        // state 가 EXPIRED 로, 변경되기 전에 release 를 호출했을 경우
        if (!moment.isBefore(expiresAt)) {
            throw OccupationAlreadyExpiredException()
        }

        status = OccupationStatus.RELEASED
        allocations.forEach { it.markAsReserved() }
    }

    private fun verifyActiveStatus() {
        if (status == OccupationStatus.EXPIRED) {
            throw OccupationAlreadyExpiredException()
        }

        if (status == OccupationStatus.RELEASED) {
            throw OccupationAlreadyReleaseException()
        }
    }

    fun copy(
        id: Long = this.id,
        userId: Long = this.userId,
        concertEventId: Long = this.concertEventId,
        allocations: List<SeatAllocation> = this.allocations,
        status: OccupationStatus = this.status,
        createdAt: ZonedDateTime = this.createdAt,
        expiresAt: ZonedDateTime = this.expiresAt,
        expiredAt: ZonedDateTime? = this.expiredAt,
    ): Occupation =
        Occupation(
            id = id,
            userId = userId,
            concertEventId = concertEventId,
            allocations = allocations,
            status = status,
            createdAt = createdAt,
            expiresAt = expiresAt,
            expiredAt = expiredAt,
        )

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
