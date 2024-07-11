package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.InvalidSeatStatusException
import java.math.BigDecimal

data class Seat(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
    var isAvailable: Boolean,
) {
    fun makeUnavailable() {
        isAvailable = false
    }

    fun checkAvailable() {
        if (!isAvailable) throw InvalidSeatStatusException()
    }
}
