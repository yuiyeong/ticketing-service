package com.yuiyeong.ticketing.domain.model

import java.math.BigDecimal

data class Seat(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val status: SeatStatus,
) {
    val isAvailable: Boolean
        get() = status == SeatStatus.AVAILABLE
}

enum class SeatStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
}
