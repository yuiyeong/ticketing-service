package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.UserWalletDto
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.stereotype.Component

@Component
class WalletBalanceUseCaseImpl(
    private val walletService: WalletService,
) : WalletBalanceUseCase {
    override fun getBalance(userId: Long): UserWalletDto {
        val wallet = walletService.getBalance(userId)
        return UserWalletDto.from(wallet)
    }
}
