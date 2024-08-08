package com.yuiyeong.ticketing.infrastructure.jpa.repository.wallet

import com.yuiyeong.ticketing.infrastructure.jpa.entity.wallet.WalletEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface WalletJpaRepository : JpaRepository<WalletEntity, Long> {
    fun findOneByUserId(userId: Long): WalletEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findOneWithLockByUserId(userId: Long): WalletEntity?
}
