package com.yuiyeong.ticketing.config.swagger.schema.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "잔액 충전 요청")
data class ChargeWalletRequest(
    @field:Schema(description = "충전할 금액", example = "50000")
    val amount: Int,
)
