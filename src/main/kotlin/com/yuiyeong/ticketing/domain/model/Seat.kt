package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.SeatAlreadyUnavailableException
import java.math.BigDecimal

data class Seat(
    val id: Long,
    val concertEventId: Long,
    val seatNumber: String,
    val price: BigDecimal,
    var isAvailable: Boolean,
) {
    fun makeUnavailable() {
        if (!isAvailable) {
            throw SeatAlreadyUnavailableException()
        }
        isAvailable = false
    }
}
