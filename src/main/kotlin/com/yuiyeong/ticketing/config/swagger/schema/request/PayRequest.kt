package com.yuiyeong.ticketing.config.swagger.schema.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "결제 요청")
data class PayRequest(
    @field:Schema(description = "결제할 reservation 의 id", example = "21")
    val reservationId: Long,
)
