package com.yuiyeong.ticketing.application.dto.concert

import com.yuiyeong.ticketing.domain.model.concert.Seat
import java.math.BigDecimal

data class SeatResult(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
) {
    companion object {
        fun from(seat: Seat): SeatResult = SeatResult(seat.id, seat.seatNumber, seat.price, seat.isAvailable)
    }
}
