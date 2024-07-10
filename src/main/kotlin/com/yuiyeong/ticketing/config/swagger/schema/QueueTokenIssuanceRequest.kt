package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "userId 를 가지는 객체")
data class QueueTokenIssuanceRequest(
    @field:Schema(description = "사용자의 id", example = "11")
    val userId: Long,
)
