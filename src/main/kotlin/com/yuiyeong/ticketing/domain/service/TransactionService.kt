package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.TransactionType
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
        val transaction =
            Transaction(
                walletId = wallet.id,
                amount = BigDecimal(amount),
                type = TransactionType.CHARGE,
                createdAt = wallet.updatedAt,
            )
        transactionRepository.save(transaction)
    }
}
