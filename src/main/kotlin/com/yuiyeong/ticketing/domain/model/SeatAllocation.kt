package com.yuiyeong.ticketing.domain.model

import java.math.BigDecimal
import java.time.ZonedDateTime

class SeatAllocation(
    val id: Long,
    val userId: Long,
    val seatId: Long,
    val seatPrice: BigDecimal,
    val seatNumber: String,
    status: AllocationStatus,
    val occupiedAt: ZonedDateTime?,
    val expiredAt: ZonedDateTime?,
    val reservedAt: ZonedDateTime?,
) {
    var status: AllocationStatus = status
        private set

    fun markAsExpired() {
        status = AllocationStatus.EXPIRED
    }

    fun markAsReserved() {
        status = AllocationStatus.RESERVED
    }

    companion object {
        fun createOccupiedOne(
            userId: Long,
            seat: Seat,
            occupiedAt: ZonedDateTime,
        ): SeatAllocation =
            SeatAllocation(
                id = 0L,
                userId = userId,
                seatId = seat.id,
                seatNumber = seat.seatNumber,
                seatPrice = seat.price,
                status = AllocationStatus.OCCUPIED,
                occupiedAt = occupiedAt,
                expiredAt = null,
                reservedAt = null,
            )
    }
}

enum class AllocationStatus {
    OCCUPIED,
    EXPIRED,
    RESERVED,
}
