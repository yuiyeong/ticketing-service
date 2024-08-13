package com.yuiyeong.ticketing.interfaces.presentation.dto.wallet

import com.yuiyeong.ticketing.application.dto.wallet.WalletResult
import java.math.BigDecimal

data class WalletResponseDto(
    val balance: BigDecimal,
) {
    companion object {
        fun from(walletResult: WalletResult): WalletResponseDto = WalletResponseDto(walletResult.balance)
    }
}
