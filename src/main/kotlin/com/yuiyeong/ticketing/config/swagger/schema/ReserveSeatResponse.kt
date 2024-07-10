package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "예약된 좌석 정보")
data class ReservedSeatData(
    @field:Schema(description = "예약 ID", example = "1")
    val id: Long,
    @field:Schema(description = "콘서트 이벤트 ID", example = "1")
    val concertEventId: Long,
    @field:Schema(description = "총 예약한 좌석 수", example = "1")
    val totalSeats: Int,
    @field:Schema(description = "총 결제 금액", example = "50000")
    val totalAmount: Int,
    @field:Schema(description = "예약된 시간", example = "2023-07-01T12:05:00Z")
    val createdAt: String,
)

@Schema(description = "좌석 예약 응답")
data class ReserveSeatResponse(
    @Schema(description = "예약된 좌석 정보")
    val data: ReservedSeatData,
)
