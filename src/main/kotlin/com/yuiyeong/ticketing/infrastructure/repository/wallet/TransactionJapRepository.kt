package com.yuiyeong.ticketing.infrastructure.repository.wallet

import com.yuiyeong.ticketing.infrastructure.entity.wallet.WalletTransactionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionJapRepository : JpaRepository<WalletTransactionEntity, Long> {
    fun findAllByWalletId(walletId: Long): List<WalletTransactionEntity>
}
