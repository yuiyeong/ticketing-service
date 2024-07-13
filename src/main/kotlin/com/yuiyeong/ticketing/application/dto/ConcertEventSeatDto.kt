package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Seat
import java.math.BigDecimal

data class ConcertEventSeatDto(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
) {
    companion object {
        fun from(seat: Seat): ConcertEventSeatDto = ConcertEventSeatDto(seat.id, seat.seatNumber, seat.price, seat.isAvailable)
    }
}
