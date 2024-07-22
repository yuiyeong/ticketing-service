package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.wallet.WalletResult

interface GetBalanceUseCase {
    fun execute(userId: Long): WalletResult
}
