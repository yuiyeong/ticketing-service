package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "예약 가능한 좌석 정보")
data class AvailableSeatData(
    @field:Schema(description = "좌석 ID", example = "1")
    val id: Long,
    @field:Schema(description = "좌석 번호", example = "12")
    val seatNumber: String,
    @field:Schema(description = "좌석 가격", example = "50000")
    val price: Int,
)

@Schema(description = "예약 가능한 좌석 목록 응답")
data class AvailableSeatsResponse(
    @Schema(description = "예약 가능한 좌석 목록")
    val list: List<AvailableSeatData>,
)
