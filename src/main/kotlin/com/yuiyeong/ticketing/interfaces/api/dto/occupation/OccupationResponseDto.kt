package com.yuiyeong.ticketing.interfaces.api.dto.occupation

import com.yuiyeong.ticketing.application.dto.occupation.OccupationResult
import java.time.ZonedDateTime

data class OccupationResponseDto(
    val id: Long,
    val expiresAt: ZonedDateTime,
) {
    companion object {
        fun from(occupationResult: OccupationResult): OccupationResponseDto =
            OccupationResponseDto(
                occupationResult.id,
                occupationResult.expiresAt,
            )
    }
}
