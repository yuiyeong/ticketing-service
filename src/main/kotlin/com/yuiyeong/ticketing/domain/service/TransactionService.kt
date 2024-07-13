package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import java.math.BigDecimal

class TransactionService(
    private val transactionRepository: TransactionRepository,
) {
    fun addChargedTransaction(
        wallet: Wallet,
        amount: Long,
    ) {
        val transaction = Transaction.createAsCharge(wallet, BigDecimal(amount))
        transactionRepository.save(transaction)
    }
}
