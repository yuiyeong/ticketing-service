package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WalletResult

interface ChargingWalletUseCase {
    fun charge(
        userId: Long,
        amount: Long,
    ): WalletResult
}
