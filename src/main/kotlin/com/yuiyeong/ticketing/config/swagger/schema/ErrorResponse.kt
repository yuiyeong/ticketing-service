package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 객체")
data class ErrorData(
    @field:Schema(description = "에러 코드", example = "internal_server_error")
    val code: String,
    @field:Schema(description = "에러 메시지", example = "서버 내부 오류가 발생했습니다.")
    val message: String,
)

@Schema(description = "에러 응답 형식")
data class ErrorResponse(
    @Schema(contentSchema = ErrorData::class)
    val error: ErrorData,
)
