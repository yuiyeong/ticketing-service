package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.infrastructure.entity.ReservationEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface ReservationJpaRepository : JpaRepository<ReservationEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findOneWithLockById(id: Long): ReservationEntity?
}
