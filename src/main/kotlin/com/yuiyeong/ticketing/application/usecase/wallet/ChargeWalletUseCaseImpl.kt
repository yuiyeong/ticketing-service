package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.WalletResult
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class ChargeWalletUseCaseImpl(
    private val walletService: WalletService,
) : ChargeWalletUseCase {
    @Transactional
    override fun execute(
        userId: Long,
        amount: Long,
    ): WalletResult {
        walletService.charge(userId, BigDecimal(amount))
        return WalletResult.from(walletService.getUserWallet(userId))
    }
}
