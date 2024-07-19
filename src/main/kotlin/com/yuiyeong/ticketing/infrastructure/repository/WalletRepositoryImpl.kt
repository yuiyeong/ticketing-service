package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.WalletRepository
import com.yuiyeong.ticketing.infrastructure.entity.WalletEntity
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
