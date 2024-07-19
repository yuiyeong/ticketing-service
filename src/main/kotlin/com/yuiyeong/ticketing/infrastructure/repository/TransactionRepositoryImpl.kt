package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import com.yuiyeong.ticketing.infrastructure.entity.WalletTransactionEntity
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

    override fun deleteAll() = transactionJapRepository.deleteAll()
}
