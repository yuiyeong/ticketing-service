package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.SeatResult
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
