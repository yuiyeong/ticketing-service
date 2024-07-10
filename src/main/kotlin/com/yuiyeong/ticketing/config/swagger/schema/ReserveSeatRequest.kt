package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "좌석 예약 요청")
data class ReserveSeatRequest(
    @field:Schema(description = "예약할 좌석 ID", example = "1234")
    val seatId: Long,
)
