package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WalletResult

interface GetBalanceUseCase {
    fun execute(userId: Long): WalletResult
}
