package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.WalletResult

interface GetBalanceUseCase {
    fun execute(userId: Long): WalletResult
}
