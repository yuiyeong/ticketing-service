package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Wallet
import java.math.BigDecimal

data class UserWalletDto(
    val userId: Long,
    val balance: BigDecimal,
) {
    companion object {
        fun from(wallet: Wallet): UserWalletDto = UserWalletDto(wallet.userId, wallet.balance)
    }
}
