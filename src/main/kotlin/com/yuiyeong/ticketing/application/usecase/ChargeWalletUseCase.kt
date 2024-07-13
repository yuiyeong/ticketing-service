package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WalletResult

interface ChargeWalletUseCase {
    fun execute(
        userId: Long,
        amount: Long,
    ): WalletResult
}
