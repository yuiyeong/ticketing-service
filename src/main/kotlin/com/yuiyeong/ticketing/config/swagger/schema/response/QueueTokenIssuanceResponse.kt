package com.yuiyeong.ticketing.config.swagger.schema.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "발급된 Token 객체")
data class TokenData(
    @field:Schema(description = "대기 정보에 대한 token", example = "a21kTkseTestToken")
    val token: String,
    @field:Schema(description = "예상 대기 시간(분)", example = "20")
    val estimatedWaitingTime: String,
)

@Schema(description = "대기열 token 발급 응답")
data class QueueTokenIssuanceResponse(
    @Schema(contentSchema = TokenData::class)
    val data: TokenData,
)
