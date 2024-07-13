package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.WalletResult
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.stereotype.Component

@Component
class GetBalanceUseCaseImpl(
    private val walletService: WalletService,
) : GetBalanceUseCase {
    override fun execute(userId: Long): WalletResult {
        val wallet = walletService.getBalance(userId)
        return WalletResult.from(wallet)
    }
}
