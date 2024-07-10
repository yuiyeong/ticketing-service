package com.yuiyeong.ticketing.config.swagger.schema.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "충전 후 잔액 정보")
data class ChargeWalletData(
    @field:Schema(description = "갱신된 잔액", example = "150000")
    val balance: Int,
)

@Schema(description = "잔액 충전 응답")
data class ChargeWalletResponse(
    @Schema(description = "충전 후 잔액 정보")
    val data: ChargeWalletData,
)
