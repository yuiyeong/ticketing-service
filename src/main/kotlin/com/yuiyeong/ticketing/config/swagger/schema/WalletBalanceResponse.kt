package com.yuiyeong.ticketing.config.swagger.schema

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "잔액 정보")
data class WalletBalanceData(
    @field:Schema(description = "현재 잔액", example = "100000")
    val balance: Int,
)

@Schema(description = "잔액 조회 응답")
data class WalletBalanceResponse(
    @Schema(description = "잔액 정보")
    val data: WalletBalanceData,
)
