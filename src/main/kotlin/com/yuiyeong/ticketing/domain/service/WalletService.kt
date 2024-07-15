package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.WalletNotFoundException
import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.TransactionType
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.WalletRepository
import java.math.BigDecimal

class WalletService(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
) {
    fun getUserWallet(userId: Long): Wallet = walletRepository.findOneByUserId(userId) ?: throw WalletNotFoundException()

    fun charge(
        userId: Long,
        amount: BigDecimal,
    ) = createTransaction(userId, amount, TransactionType.CHARGE)

    fun pay(
        userId: Long,
        amount: BigDecimal,
    ) = createTransaction(userId, amount, TransactionType.PAYMENT)

    private fun createTransaction(
        userId: Long,
        amount: BigDecimal,
        type: TransactionType,
    ): Transaction {
        val wallet = getUserWallet(userId)
        val transaction = Transaction.create(wallet, amount, type)

        when (type) {
            TransactionType.CHARGE -> wallet.charge(amount)
            TransactionType.PAYMENT -> wallet.pay(amount)
        }
        walletRepository.save(wallet)
        return transactionRepository.save(transaction)
    }
}
