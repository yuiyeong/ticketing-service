package com.yuiyeong.ticketing.infrastructure.jpa.repository.reservation

import com.yuiyeong.ticketing.infrastructure.jpa.entity.reservation.ReservationEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface ReservationJpaRepository : JpaRepository<ReservationEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findOneWithLockById(id: Long): ReservationEntity?
}
