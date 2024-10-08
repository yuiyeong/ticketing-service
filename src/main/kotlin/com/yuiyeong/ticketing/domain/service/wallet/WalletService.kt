package com.yuiyeong.ticketing.domain.service.wallet

import com.yuiyeong.ticketing.domain.exception.WalletNotFoundException
import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import com.yuiyeong.ticketing.domain.model.wallet.TransactionType
import com.yuiyeong.ticketing.domain.model.wallet.Wallet
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.wallet.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
) {
    @Transactional(readOnly = true)
    fun getUserWallet(userId: Long): Wallet = walletRepository.findOneByUserId(userId) ?: throw WalletNotFoundException()

    @Transactional
    fun charge(
        userId: Long,
        amount: BigDecimal,
    ) = createTransaction(userId, amount, TransactionType.CHARGE)

    @Transactional
    fun pay(
        userId: Long,
        amount: BigDecimal,
    ) = createTransaction(userId, amount, TransactionType.PAYMENT)

    @Transactional
    fun paySafely(
        userId: Long,
        amount: BigDecimal,
    ): Result<Transaction> = runCatching { createTransaction(userId, amount, TransactionType.PAYMENT) }

    private fun createTransaction(
        userId: Long,
        amount: BigDecimal,
        type: TransactionType,
    ): Transaction {
        val wallet = walletRepository.findOneByUserIdWithLock(userId) ?: throw WalletNotFoundException()
        val updatedWallet =
            walletRepository.save(
                when (type) {
                    TransactionType.CHARGE -> wallet.charge(amount)
                    TransactionType.PAYMENT -> wallet.pay(amount)
                },
            )
        return transactionRepository.save(Transaction.create(updatedWallet, amount, type))
    }
}
