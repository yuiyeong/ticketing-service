package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Transaction

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction

    fun findOneById(id: Long): Transaction?
}
