package com.yuiyeong.ticketing.presentation.dto.concert

import com.yuiyeong.ticketing.application.dto.concert.SeatResult
import java.math.BigDecimal

data class SeatResponseDto(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
) {
    companion object {
        fun from(concertEventSeat: SeatResult): SeatResponseDto =
            SeatResponseDto(
                concertEventSeat.id,
                concertEventSeat.seatNumber,
                concertEventSeat.price,
            )
    }
}
