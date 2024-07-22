package com.yuiyeong.ticketing.domain.service.queue

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import com.yuiyeong.ticketing.domain.repository.queue.QueueEntryRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class QueueService(
    private val entryRepository: QueueEntryRepository,
) {
    fun getFirstWaitingPosition(): Long = entryRepository.findFirstWaitingPosition() ?: 0

    fun enter(
        userId: Long,
        token: String,
        enteredAt: ZonedDateTime,
        expiresAt: ZonedDateTime,
    ): QueueEntry {
        val lastPosition = entryRepository.findLastWaitingPosition() ?: 0
        val activeSize = entryRepository.findAllByStatus(QueueEntryStatus.PROCESSING).size
        val status = if (activeSize < MAX_ACTIVE_ENTRIES) QueueEntryStatus.PROCESSING else QueueEntryStatus.WAITING
        val newPosition = if (status == QueueEntryStatus.WAITING) lastPosition + 1 else 0

        val queueEntry = QueueEntry.create(userId, newPosition, token, status, enteredAt, expiresAt)
        return entryRepository.save(queueEntry)
    }

    fun exit(entryId: Long): QueueEntry {
        val entry = entryRepository.findOneByIdWithLock(entryId) ?: throw InvalidTokenException()
        val current = ZonedDateTime.now().asUtc
        entry.exit(current)
        return entryRepository.save(entry)
    }

    fun getEntry(token: String?): QueueEntry {
        if (token == null) throw InvalidTokenException()
        return entryRepository.findOneByToken(token) ?: throw InvalidTokenException()
    }

    fun activateWaitingEntries(): List<QueueEntry> {
        val alreadyActivatedEntries = entryRepository.findAllByStatus(QueueEntryStatus.PROCESSING)
        val newActivatingCount = MAX_ACTIVE_ENTRIES - alreadyActivatedEntries.count()
        if (newActivatingCount <= 0) {
            return emptyList() // there is no one to be activated.
        }

        val current = ZonedDateTime.now().asUtc
        val waitingEntries =
            entryRepository.findAllByStatusOrderByPositionWithLock(QueueEntryStatus.WAITING, newActivatingCount)
        waitingEntries.forEach { it.process(current) }

        return entryRepository.saveAll(waitingEntries)
    }

    fun expireOverdueEntries(): List<QueueEntry> {
        val current = ZonedDateTime.now().asUtc
        val entries =
            entryRepository.findAllByExpiresAtBeforeAndStatusWithLock(
                current,
                QueueEntryStatus.PROCESSING,
                QueueEntryStatus.WAITING,
            )
        entries.forEach { it.expire(current) }
        return entryRepository.saveAll(entries)
    }

    fun dequeueExistingEntries(userId: Long) {
        entryRepository
            .findAllByUserIdWithStatus(userId, QueueEntryStatus.PROCESSING, QueueEntryStatus.WAITING)
            .forEach { exit(it.id) }
    }

    companion object {
        const val MAX_ACTIVE_ENTRIES = 10 // 최대 10 명까지 작업 가능함
    }
}
