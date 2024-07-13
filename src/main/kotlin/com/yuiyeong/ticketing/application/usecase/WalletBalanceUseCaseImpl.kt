package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WalletResult
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.stereotype.Component

@Component
class WalletBalanceUseCaseImpl(
    private val walletService: WalletService,
) : WalletBalanceUseCase {
    override fun getBalance(userId: Long): WalletResult {
        val wallet = walletService.getBalance(userId)
        return WalletResult.from(wallet)
    }
}
