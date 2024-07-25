package com.yuiyeong.ticketing.domain.model.occupation

import com.yuiyeong.ticketing.domain.model.concert.Seat
import java.math.BigDecimal
import java.time.ZonedDateTime

data class SeatAllocation(
    val id: Long,
    val userId: Long,
    val seatId: Long,
    val seatPrice: BigDecimal,
    val seatNumber: String,
    val status: AllocationStatus,
    val occupiedAt: ZonedDateTime?,
    val expiredAt: ZonedDateTime?,
    val reservedAt: ZonedDateTime?,
) {
    fun markAsExpired(): SeatAllocation = copy(status = AllocationStatus.EXPIRED)

    fun markAsReserved(): SeatAllocation = copy(status = AllocationStatus.RESERVED)

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
