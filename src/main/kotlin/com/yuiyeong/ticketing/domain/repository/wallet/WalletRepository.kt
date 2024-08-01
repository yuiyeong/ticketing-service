package com.yuiyeong.ticketing.domain.repository.wallet

import com.yuiyeong.ticketing.domain.model.wallet.Wallet

interface WalletRepository {
    fun save(wallet: Wallet): Wallet

    fun findOneByUserId(userId: Long): Wallet?

    fun findOneByUserIdWithLock(userId: Long): Wallet?
}
