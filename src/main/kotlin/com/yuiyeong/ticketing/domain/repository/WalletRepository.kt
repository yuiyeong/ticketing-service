package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Wallet

interface WalletRepository {
    fun save(wallet: Wallet): Wallet

    fun findOneByUserIdWithLock(userId: Long): Wallet?

    fun deleteAll()
}
