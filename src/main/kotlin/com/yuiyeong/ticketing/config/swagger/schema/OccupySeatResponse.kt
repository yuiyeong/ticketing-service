package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "점유된 좌석 정보")
data class OccupiedSeatData(
    @field:Schema(description = "점유한 좌석의 ID", example = "1")
    val id: Long,
    @field:Schema(description = "점유한 좌석의 좌석 번호", example = "50")
    val seatNumber: String,
    @field:Schema(description = "점유한 좌석의 가격", example = "50000")
    val price: Int,
    @field:Schema(description = "점유 만료 시간", example = "2023-07-01T12:05:00Z")
    val expirationTime: String,
)

@Schema(description = "좌석 점유 응답")
data class OccupySeatResponse(
    @Schema(description = "점유된 좌석 정보")
    val data: OccupiedSeatData,
)
