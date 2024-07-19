package com.yuiyeong.ticketing.config.swagger.schema.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "좌석 점유 요청")
data class OccupySeatRequest(
    @field:Schema(description = "점유할 좌석 ID", example = "1")
    val seatId: Long,
)
