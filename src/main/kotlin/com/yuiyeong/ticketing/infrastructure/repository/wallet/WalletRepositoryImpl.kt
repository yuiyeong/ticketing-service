package com.yuiyeong.ticketing.infrastructure.repository.wallet

import com.yuiyeong.ticketing.domain.model.wallet.Wallet
import com.yuiyeong.ticketing.domain.repository.wallet.WalletRepository
import com.yuiyeong.ticketing.infrastructure.entity.wallet.WalletEntity
import org.springframework.stereotype.Repository

@Repository
class WalletRepositoryImpl(
    private val walletJpaRepository: WalletJpaRepository,
) : WalletRepository {
    override fun save(wallet: Wallet): Wallet {
        val walletEntity = walletJpaRepository.save(WalletEntity.from(wallet))
        return walletEntity.toWallet()
    }

    override fun findOneByUserIdWithLock(userId: Long): Wallet? = walletJpaRepository.findOneWithLockByUserId(userId)?.toWallet()

    override fun deleteAll() = walletJpaRepository.deleteAll()
}
