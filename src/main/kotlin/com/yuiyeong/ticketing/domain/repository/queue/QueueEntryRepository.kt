package com.yuiyeong.ticketing.domain.repository.queue

import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import java.time.ZonedDateTime

interface QueueEntryRepository {
    fun save(entry: QueueEntry): QueueEntry

    fun saveAll(entries: List<QueueEntry>): List<QueueEntry>

    fun findOneByToken(token: String): QueueEntry?

    fun findOneByIdWithLock(id: Long): QueueEntry?

    fun findAllByUserIdWithStatus(
        userId: Long,
        vararg status: QueueEntryStatus,
    ): List<QueueEntry>

    fun findAllByStatus(vararg status: QueueEntryStatus): List<QueueEntry>

    fun findAllByStatusOrderByPositionWithLock(
        status: QueueEntryStatus,
        limit: Int,
    ): List<QueueEntry>

    fun findAllByExpiresAtBeforeAndStatusWithLock(
        moment: ZonedDateTime,
        vararg status: QueueEntryStatus,
    ): List<QueueEntry>

    fun findLastWaitingPosition(): Long?

    fun findFirstWaitingPosition(): Long?

    fun deleteAll()
}
