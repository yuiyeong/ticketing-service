package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.infrastructure.entity.SeatEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SeatJpaRepository : JpaRepository<SeatEntity, Long> {
    @Query("SELECT s FROM SeatEntity s WHERE s.id IN :ids")
    fun findAllByIds(
        @Param("ids") ids: List<Long>,
    ): List<SeatEntity>

    @Query("SELECT s FROM SeatEntity s WHERE s.id IN :ids AND s.isAvailable = true")
    fun findAllAvailableByIds(
        @Param("ids") ids: List<Long>,
    ): List<SeatEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s WHERE s.id IN :ids AND s.isAvailable = true")
    fun findAllAvailableWithLockByIds(
        @Param("ids") ids: List<Long>,
    ): List<SeatEntity>

    @Query("SELECT s FROM SeatEntity s WHERE s.concertEventId = :concertEventId AND s.isAvailable = true")
    fun findAllAvailableByConcertEventId(
        @Param("concertEventId") concertEventId: Long,
    ): List<SeatEntity>
}
