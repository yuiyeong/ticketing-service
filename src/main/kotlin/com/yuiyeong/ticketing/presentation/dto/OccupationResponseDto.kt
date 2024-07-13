package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.OccupationResult
import java.math.BigDecimal
import java.time.ZonedDateTime

data class OccupationResponseDto(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val expiresAt: ZonedDateTime,
) {
    companion object {
        fun from(occupationResult: OccupationResult): OccupationResponseDto =
            OccupationResponseDto(
                occupationResult.id,
                occupationResult.seatNumber,
                occupationResult.price,
                occupationResult.expiresAt,
            )
    }
}
