package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.OccupationDto
import java.math.BigDecimal
import java.time.ZonedDateTime

data class OccupiedSeatDto(
    val id: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val expiresAt: ZonedDateTime,
) {
    companion object {
        fun from(occupationDto: OccupationDto): OccupiedSeatDto =
            OccupiedSeatDto(
                occupationDto.id,
                occupationDto.seatNumber,
                occupationDto.price,
                occupationDto.expiresAt,
            )
    }
}
