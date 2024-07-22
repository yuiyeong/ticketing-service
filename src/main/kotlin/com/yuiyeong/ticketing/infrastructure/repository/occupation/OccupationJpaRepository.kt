package com.yuiyeong.ticketing.infrastructure.repository.occupation

import com.yuiyeong.ticketing.infrastructure.entity.occupation.OccupationEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime

interface OccupationJpaRepository : JpaRepository<OccupationEntity, Long> {
    @EntityGraph(attributePaths = ["seatAllocations"])
    @Query("SELECT o FROM OccupationEntity o WHERE o.id = :occupationId")
    fun findOneById(
        @Param("occupationId") occupationId: Long,
    ): OccupationEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = ["seatAllocations"])
    @Query("SELECT o FROM OccupationEntity o WHERE o.id = :occupationId")
    fun findOneWithLockById(
        @Param("occupationId") occupationId: Long,
    ): OccupationEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = ["seatAllocations"])
    @Query("SELECT o from OccupationEntity o WHERE o.expiresAt < :moment")
    fun findAllWithLockByExpiresAtBefore(
        @Param("moment") moment: ZonedDateTime,
    ): List<OccupationEntity>
}
