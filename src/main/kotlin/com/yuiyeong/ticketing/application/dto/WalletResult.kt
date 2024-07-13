package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.Wallet
import java.math.BigDecimal

data class WalletResult(
    val userId: Long,
    val balance: BigDecimal,
) {
    companion object {
        fun from(wallet: Wallet): WalletResult = WalletResult(wallet.userId, wallet.balance)
    }
}
