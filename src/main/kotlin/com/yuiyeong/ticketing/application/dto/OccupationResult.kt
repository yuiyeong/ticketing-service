package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Occupation
import com.yuiyeong.ticketing.domain.model.OccupationStatus
import java.math.BigDecimal
import java.time.ZonedDateTime

data class OccupationResult(
    val id: Long,
    val userId: Long,
    val seatId: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val status: OccupationStatus,
    val expiresAt: ZonedDateTime,
) {
    companion object {
        fun from(occupation: Occupation): OccupationResult =
            OccupationResult(
                occupation.id,
                occupation.userId,
                occupation.seat.id,
                occupation.seat.seatNumber,
                occupation.seat.price,
                occupation.status,
                occupation.expiresAt,
            )
    }
}
