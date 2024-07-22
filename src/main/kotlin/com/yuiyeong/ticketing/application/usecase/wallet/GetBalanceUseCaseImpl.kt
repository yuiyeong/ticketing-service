package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.wallet.WalletResult
import com.yuiyeong.ticketing.domain.service.wallet.WalletService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetBalanceUseCaseImpl(
    private val walletService: WalletService,
) : GetBalanceUseCase {
    @Transactional(readOnly = true)
    override fun execute(userId: Long): WalletResult {
        val wallet = walletService.getUserWallet(userId)
        return WalletResult.from(wallet)
    }
}
