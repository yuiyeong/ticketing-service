package com.yuiyeong.ticketing.infrastructure.repository.wallet

import com.yuiyeong.ticketing.domain.model.wallet.Transaction
import com.yuiyeong.ticketing.domain.repository.wallet.TransactionRepository
import com.yuiyeong.ticketing.infrastructure.entity.wallet.WalletTransactionEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class TransactionRepositoryImpl(
    private val transactionJapRepository: TransactionJapRepository,
) : TransactionRepository {
    override fun save(transaction: Transaction): Transaction {
        val walletTransactionEntity = transactionJapRepository.save(WalletTransactionEntity.from(transaction))
        return walletTransactionEntity.toTransaction()
    }

    override fun findOneById(id: Long): Transaction? = transactionJapRepository.findByIdOrNull(id)?.toTransaction()

    override fun findAllByWalletId(walletId: Long): List<Transaction> =
        transactionJapRepository.findAllByWalletId(walletId).map {
            it.toTransaction()
        }
}
