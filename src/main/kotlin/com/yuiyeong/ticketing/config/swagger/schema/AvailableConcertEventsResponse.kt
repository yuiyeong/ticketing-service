package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "예약 가능한 콘서트 이벤트 정보")
data class AvailableConcertEventData(
    @field:Schema(description = "콘서트 이벤트의 id", example = "1")
    val id: Long,
    @field:Schema(description = "예약 가능한 날짜", example = "2023-07-01")
    val date: String,
)

@Schema(description = "예약 가능한 콘서트 이벤트 목록 응답")
data class AvailableConcertEventsResponse(
    @Schema(description = "예약 가능한 콘서트 이벤트 목록")
    val list: List<AvailableConcertEventData>,
)
