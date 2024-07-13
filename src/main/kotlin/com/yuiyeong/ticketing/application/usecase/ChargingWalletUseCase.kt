package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.UserWalletDto

interface ChargingWalletUseCase {
    fun charge(
        userId: Long,
        amount: Long,
    ): UserWalletDto
}
