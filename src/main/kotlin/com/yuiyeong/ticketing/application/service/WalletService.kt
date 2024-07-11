package com.yuiyeong.ticketing.application.service

import com.yuiyeong.ticketing.application.dto.UserWalletDto
import com.yuiyeong.ticketing.application.usecase.WalletUseCase
import com.yuiyeong.ticketing.domain.exception.NotFoundWalletException
import com.yuiyeong.ticketing.domain.model.Transaction
import com.yuiyeong.ticketing.domain.model.TransactionType
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.TransactionRepository
import com.yuiyeong.ticketing.domain.repository.WalletRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
) : WalletUseCase {
    override fun charge(
        userId: Long,
        amount: Long,
    ): UserWalletDto {
        val wallet = walletRepository.findOneByUserId(userId) ?: throw NotFoundWalletException()
        wallet.charge(amount)
        return UserWalletDto.from(
            walletRepository.save(wallet).also { addChargedTransaction(it, amount) },
        )
    }

    override fun getBalance(userId: Long): UserWalletDto {
        val wallet = walletRepository.findOneByUserId(userId) ?: throw NotFoundWalletException()
        return UserWalletDto.from(wallet)
    }

    private fun addChargedTransaction(
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
