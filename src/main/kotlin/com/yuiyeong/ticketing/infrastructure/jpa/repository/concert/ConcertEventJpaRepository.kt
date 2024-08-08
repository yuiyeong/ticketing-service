package com.yuiyeong.ticketing.infrastructure.jpa.repository.concert

import com.yuiyeong.ticketing.infrastructure.jpa.entity.concert.ConcertEventEntity
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime

interface ConcertEventJpaRepository : JpaRepository<ConcertEventEntity, Long> {
    @Query(
        """ 
        SELECT ce FROM ConcertEventEntity ce 
        WHERE ce.concert.id = :concertId 
        AND ce.reservationStartAt <= :moment 
        AND :moment <= ce.reservationEndAt
        """,
    )
    fun findAllWithinPeriodByConcertId(
        @Param("concertId") concertId: Long,
        @Param("moment") moment: ZonedDateTime,
    ): List<ConcertEventEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    fun findOneWithLockById(id: Long): ConcertEventEntity?
}
