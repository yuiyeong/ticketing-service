package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.infrastructure.entity.WalletTransactionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionJapRepository : JpaRepository<WalletTransactionEntity, Long>
