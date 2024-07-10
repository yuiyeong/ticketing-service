package com.yuiyeong.ticketing.config.swagger.schema.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "대기열 상태 정보 객체")
data class QueueStatusData(
    @field:Schema(description = "대기열에서의 현재 위치", example = "15")
    val queuePosition: Int,
    @field:Schema(description = "예상 대기 시간(분)", example = "10")
    val estimatedWaitingTime: Int,
)

@Schema(description = "대기열 상태 조회 응답")
data class QueueStatusResponse(
    @Schema(contentSchema = QueueStatusData::class)
    val data: QueueStatusData,
)
