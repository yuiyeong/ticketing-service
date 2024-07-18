package com.yuiyeong.ticketing.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.QueueEntry
import com.yuiyeong.ticketing.domain.model.QueueEntryStatus
import com.yuiyeong.ticketing.domain.repository.QueueEntryRepository
import com.yuiyeong.ticketing.infrastructure.entity.QueueEntryEntity
import com.yuiyeong.ticketing.infrastructure.entity.QueueEntryEntityStatus
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
class QueueEntryRepositoryImpl(
    private val queueEntryJpaRepository: QueueEntryJpaRepository,
) : QueueEntryRepository {
    override fun save(entry: QueueEntry): QueueEntry {
        val entity = queueEntryJpaRepository.save(QueueEntryEntity.from(entry))
        return entity.toQueueEntry()
    }

    override fun saveAll(entries: List<QueueEntry>): List<QueueEntry> {
        val entities = queueEntryJpaRepository.saveAll(entries.map { QueueEntryEntity.from(it) })
        return entities.map { it.toQueueEntry() }
    }

    override fun findOneByToken(token: String): QueueEntry? = queueEntryJpaRepository.findOneByToken(token)?.toQueueEntry()

    override fun findOneByIdWithLock(id: Long): QueueEntry? = queueEntryJpaRepository.findOneWithLockById(id)?.toQueueEntry()

    override fun findAllByUserIdWithStatus(
        userId: Long,
        vararg status: QueueEntryStatus,
    ): List<QueueEntry> {
        val entityStatuses = status.map { QueueEntryEntityStatus.from(it) }.toTypedArray()
        val entities = queueEntryJpaRepository.findAllByUserIdAndStatuses(userId, entityStatuses)
        return entities.map { it.toQueueEntry() }
    }

    override fun findAllByStatus(vararg status: QueueEntryStatus): List<QueueEntry> {
        val entityStatuses = status.map { QueueEntryEntityStatus.from(it) }.toTypedArray()
        val entities = queueEntryJpaRepository.findAllByStatuses(entityStatuses)
        return entities.map { it.toQueueEntry() }
    }

    override fun findAllByStatusOrderByPositionWithLock(
        status: QueueEntryStatus,
        limit: Int,
    ): List<QueueEntry> {
        val limitPageable = PageRequest.of(0, limit)
        val entities = queueEntryJpaRepository.findAllByStatusOrderByPositionWithLock(QueueEntryEntityStatus.from(status), limitPageable)
        return entities.map { it.toQueueEntry() }
    }

    override fun findAllByExpiresAtBeforeAndStatusWithLock(
        moment: ZonedDateTime,
        vararg status: QueueEntryStatus,
    ): List<QueueEntry> {
        val entityStatuses = status.map { QueueEntryEntityStatus.from(it) }.toTypedArray()
        val entities = queueEntryJpaRepository.findAllByStatusAndExpiresAtBeforeWithLock(entityStatuses, moment)
        return entities.map { it.toQueueEntry() }
    }

    override fun findLastWaitingPosition(): Long? = queueEntryJpaRepository.findLastPositionByStatus(QueueEntryEntityStatus.READY)

    override fun findFirstWaitingPosition(): Long? = queueEntryJpaRepository.findFirstPositionByStatus(QueueEntryEntityStatus.READY)

    override fun deleteAll() = queueEntryJpaRepository.deleteAll()
}
