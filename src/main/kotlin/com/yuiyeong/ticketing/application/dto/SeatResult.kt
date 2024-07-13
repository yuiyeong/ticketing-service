package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Seat
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
