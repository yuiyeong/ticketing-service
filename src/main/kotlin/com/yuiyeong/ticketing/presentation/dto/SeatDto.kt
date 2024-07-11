package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.ConcertEventSeatDto
import java.math.BigDecimal

data class SeatDto(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
) {
    companion object {
        fun from(concertEventSeat: ConcertEventSeatDto): SeatDto =
            SeatDto(concertEventSeat.id, concertEventSeat.seatNumber, concertEventSeat.price)
    }
}
