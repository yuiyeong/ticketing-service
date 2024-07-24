package com.yuiyeong.ticketing.domain.repository.wallet

import com.yuiyeong.ticketing.domain.model.wallet.Transaction

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction

    fun findOneById(id: Long): Transaction?

    fun findAllByWalletId(walletId: Long): List<Transaction>

    fun deleteAll()
}
