package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Seat
import com.yuiyeong.ticketing.domain.model.SeatStatus
import java.math.BigDecimal

data class ConcertEventSeatDto(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val status: SeatStatus,
) {
    companion object {
        fun from(seat: Seat): ConcertEventSeatDto = ConcertEventSeatDto(seat.id, seat.seatNumber, seat.price, seat.status)
    }
}
