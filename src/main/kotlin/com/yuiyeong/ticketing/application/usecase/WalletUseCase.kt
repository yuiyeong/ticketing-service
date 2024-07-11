package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.UserWalletDto

interface WalletUseCase {
    fun charge(
        userId: Long,
        amount: Long,
    ): UserWalletDto

    fun getBalance(userId: Long): UserWalletDto
}
