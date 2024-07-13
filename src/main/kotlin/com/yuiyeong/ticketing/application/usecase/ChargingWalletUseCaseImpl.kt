package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.UserWalletDto
import com.yuiyeong.ticketing.domain.service.TransactionService
import com.yuiyeong.ticketing.domain.service.WalletService
import org.springframework.stereotype.Component

@Component
class ChargingWalletUseCaseImpl(
    private val walletService: WalletService,
    private val transactionService: TransactionService,
) : ChargingWalletUseCase {
    override fun charge(
        userId: Long,
        amount: Long,
    ): UserWalletDto {
        val wallet = walletService.charge(userId, amount)
        transactionService.addChargedTransaction(wallet, amount)
        return UserWalletDto.from(wallet)
    }
}
