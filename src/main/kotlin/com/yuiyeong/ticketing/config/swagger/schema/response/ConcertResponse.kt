package com.yuiyeong.ticketing.config.swagger.schema.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "콘서트 정보")
data class ConcertData(
    @field:Schema(description = "콘서트 ID", example = "1")
    val id: Long,
    @field:Schema(description = "콘서트 제목", example = "오랜만에 돌아온 트로트의 황제")
    val title: String,
    @field:Schema(description = "가수명", example = "나훈아")
    val singer: String,
    @field:Schema(description = "콘서트 설명", example = "데뷔 30주년 기념 콘서트")
    val description: String,
)

@Schema(description = "콘서트 목록 응답")
data class ConcertResponse(
    @Schema(description = "콘서트 목록")
    val list: List<ConcertData>,
)
