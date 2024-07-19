package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.infrastructure.entity.QueueEntryEntity
import com.yuiyeong.ticketing.infrastructure.entity.QueueEntryEntityStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime

interface QueueEntryJpaRepository : JpaRepository<QueueEntryEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findOneWithLockById(id: Long): QueueEntryEntity?

    fun findOneByToken(token: String): QueueEntryEntity?

    @Query(
        """
        SELECT e FROM QueueEntryEntity e 
        WHERE e.userId = :userId 
        AND e.status in :statuses
    """,
    )
    fun findAllByUserIdAndStatuses(
        @Param("userId") userId: Long,
        @Param("statuses") statuses: Array<QueueEntryEntityStatus>,
    ): List<QueueEntryEntity>

    @Query("SELECT e FROM QueueEntryEntity e WHERE e.status in :statuses")
    fun findAllByStatuses(
        @Param("statuses") statuses: Array<QueueEntryEntityStatus>,
    ): List<QueueEntryEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QueueEntryEntity q WHERE q.status = :status ORDER BY q.queuePosition ASC")
    fun findAllByStatusOrderByPositionWithLock(
        @Param("status") status: QueueEntryEntityStatus,
        pageable: Pageable,
    ): List<QueueEntryEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT e FROM QueueEntryEntity e 
        WHERE e.status in :statuses
        AND e.expiresAt < :moment
    """,
    )
    fun findAllByStatusAndExpiresAtBeforeWithLock(
        @Param("statuses") statuses: Array<QueueEntryEntityStatus>,
        @Param("moment") moment: ZonedDateTime,
    ): List<QueueEntryEntity>

    @Query("SELECT MAX(q.queuePosition) FROM QueueEntryEntity q WHERE q.status = :status")
    fun findLastPositionByStatus(status: QueueEntryEntityStatus): Long?

    @Query("SELECT MIN(q.queuePosition) FROM QueueEntryEntity q WHERE q.status = :status")
    fun findFirstPositionByStatus(status: QueueEntryEntityStatus): Long?
}
