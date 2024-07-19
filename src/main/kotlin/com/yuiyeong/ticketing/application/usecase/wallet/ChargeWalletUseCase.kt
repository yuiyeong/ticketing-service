package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.WalletResult

interface ChargeWalletUseCase {
    fun execute(
        userId: Long,
        amount: Long,
    ): WalletResult
}
