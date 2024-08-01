package com.yuiyeong.ticketing.application.usecase.wallet

import com.yuiyeong.ticketing.application.dto.wallet.WalletResult
import com.yuiyeong.ticketing.domain.exception.WalletUnavailableException
import com.yuiyeong.ticketing.domain.service.lock.DistributedLockService
import com.yuiyeong.ticketing.domain.service.lock.LockKeyGenerator
import com.yuiyeong.ticketing.domain.service.wallet.WalletService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class ChargeWalletUseCaseImpl(
    private val walletService: WalletService,
    private val distributedLockService: DistributedLockService,
    private val lockKeyGenerator: LockKeyGenerator,
) : ChargeWalletUseCase {
    @Transactional
    override fun execute(
        userId: Long,
        amount: Long,
    ): WalletResult {
        distributedLockService.withLockByWaiting(lockKeyGenerator.generateUserWalletKey(userId)) {
            walletService.charge(userId, BigDecimal(amount))
        } ?: throw WalletUnavailableException()
        return WalletResult.from(walletService.getUserWallet(userId))
    }
}
