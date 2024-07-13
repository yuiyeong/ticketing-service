package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.WalletNotFoundException
import com.yuiyeong.ticketing.domain.model.Wallet
import com.yuiyeong.ticketing.domain.repository.WalletRepository

class WalletService(
    private val walletRepository: WalletRepository,
) {
    fun charge(
        userId: Long,
        amount: Long,
    ): Wallet {
        val wallet = walletRepository.findOneByUserId(userId) ?: throw WalletNotFoundException()
        wallet.charge(amount)
        return walletRepository.save(wallet)
    }

    fun getBalance(userId: Long): Wallet = walletRepository.findOneByUserId(userId) ?: throw WalletNotFoundException()
}
