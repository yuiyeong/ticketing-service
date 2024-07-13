package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WalletResult

interface WalletBalanceUseCase {
    fun getBalance(userId: Long): WalletResult
}
