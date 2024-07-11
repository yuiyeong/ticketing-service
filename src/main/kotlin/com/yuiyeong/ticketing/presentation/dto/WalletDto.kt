package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.UserWalletDto
import java.math.BigDecimal

data class WalletDto(
    val balance: BigDecimal,
) {
    companion object {
        fun from(userWalletDto: UserWalletDto): WalletDto = WalletDto(userWalletDto.balance)
    }
}
