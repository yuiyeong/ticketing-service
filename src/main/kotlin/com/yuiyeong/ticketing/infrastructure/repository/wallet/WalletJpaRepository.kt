package com.yuiyeong.ticketing.infrastructure.repository.wallet

import com.yuiyeong.ticketing.infrastructure.entity.wallet.WalletEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface WalletJpaRepository : JpaRepository<WalletEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findOneWithLockByUserId(userId: Long): WalletEntity?
}
