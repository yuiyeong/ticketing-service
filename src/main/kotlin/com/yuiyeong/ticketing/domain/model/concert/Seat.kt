package com.yuiyeong.ticketing.domain.model.concert

import com.yuiyeong.ticketing.domain.exception.SeatAlreadyUnavailableException
import java.math.BigDecimal

data class Seat(
    val id: Long,
    val concertEventId: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
) {
    fun makeUnavailable(): Seat {
        if (!isAvailable) {
            throw SeatAlreadyUnavailableException()
        }
        return copy(isAvailable = false)
    }
}
