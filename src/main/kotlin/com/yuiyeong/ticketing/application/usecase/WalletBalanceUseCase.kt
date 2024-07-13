package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.UserWalletDto

interface WalletBalanceUseCase {
    fun getBalance(userId: Long): UserWalletDto
}
