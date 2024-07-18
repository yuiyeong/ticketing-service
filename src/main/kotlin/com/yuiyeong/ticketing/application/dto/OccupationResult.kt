package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Occupation
import com.yuiyeong.ticketing.domain.model.OccupationStatus
import java.time.ZonedDateTime

data class OccupationResult(
    val id: Long,
    val userId: Long,
    val seatId: Long,
    val status: OccupationStatus,
    val expiresAt: ZonedDateTime,
) {
    companion object {
        fun from(occupation: Occupation): OccupationResult =
            OccupationResult(
                occupation.id,
                occupation.userId,
                occupation.allocations[0].seatId,
                occupation.status,
                occupation.expiresAt,
            )
    }
}
