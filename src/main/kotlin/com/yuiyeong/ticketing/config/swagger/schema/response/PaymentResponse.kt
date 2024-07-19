package com.yuiyeong.ticketing.config.swagger.schema.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "결제 결과 응답")
data class PaymentResponse(
    @Schema(description = "결제 내역")
    val data: PaymentData,
)
