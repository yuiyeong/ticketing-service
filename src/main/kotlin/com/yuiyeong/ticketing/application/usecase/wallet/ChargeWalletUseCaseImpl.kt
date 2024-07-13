package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.WalletResult
import com.yuiyeong.ticketing.domain.service.TransactionService
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.stereotype.Component

@Component
class ChargeWalletUseCaseImpl(
    private val walletService: WalletService,
    private val transactionService: TransactionService,
) : ChargeWalletUseCase {
    override fun execute(
        userId: Long,
        amount: Long,
    ): WalletResult {
        val wallet = walletService.charge(userId, amount)
        transactionService.addChargedTransaction(wallet, amount)
        return WalletResult.from(wallet)
    }
}
