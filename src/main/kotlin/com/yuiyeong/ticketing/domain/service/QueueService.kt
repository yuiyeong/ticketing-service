package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.WaitingEntry
import com.yuiyeong.ticketing.domain.model.WaitingEntryStatus
import com.yuiyeong.ticketing.domain.repository.WaitingEntryRepository
import java.time.ZonedDateTime

class QueueService(
    private val entryRepository: WaitingEntryRepository,
) {
    fun getFirstWaitingPosition(): Long = entryRepository.findFirstWaitingPosition() ?: 0

    fun enter(userId: Long): WaitingEntry {
        // 기존에 발급 받은 token 이 있다면, 대기열에 제거
        dequeueExistingEntries(userId)

        val lastPosition = entryRepository.findLastWaitingPosition() ?: 0
        val activeSize = entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING).size
        val status = if (activeSize < MAX_ACTIVE_ENTRIES) WaitingEntryStatus.PROCESSING else WaitingEntryStatus.WAITING
        val newPosition = if (status == WaitingEntryStatus.WAITING) lastPosition + 1 else 0

        return entryRepository.save(WaitingEntry.create(userId, newPosition, status))
    }

    fun exit(token: String): WaitingEntry {
        val entry = entryRepository.findOneByToken(token) ?: throw InvalidTokenException()
        entry.exit()
        return entryRepository.save(entry)
    }

    fun getEntryInfo(token: String): WaitingEntry = entryRepository.findOneByToken(token) ?: throw InvalidTokenException()

    fun activateWaitingEntries(): List<WaitingEntry> {
        val alreadyActivatedEntries = entryRepository.findAllByStatus(WaitingEntryStatus.PROCESSING)
        val newActivatingCount = MAX_ACTIVE_ENTRIES - alreadyActivatedEntries.count()
        if (newActivatingCount <= 0) {
            return emptyList() // there is no one to be activated.
        }

        val waitingEntries = entryRepository.findAllByStatusOrderByPosition(WaitingEntryStatus.WAITING, newActivatingCount)
        waitingEntries.forEach { it.process() }

        return entryRepository.saveAll(waitingEntries)
    }

    fun expireOverdueEntries(): List<WaitingEntry> {
        val current = ZonedDateTime.now()
        val entries = entryRepository.findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
        entries.forEach { it.expire(current) }
        return entryRepository.saveAll(entries)
    }

    /**
     * 대기열에 있는 userId 에 해당하는 entry 를 제거
     */
    private fun dequeueExistingEntries(userId: Long) {
        entryRepository
            .findAllByUserIdWithStatus(userId, WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
            .forEach { exit(it.token) }
    }

    companion object {
        const val MAX_ACTIVE_ENTRIES = 10 // 최대 10 명까지 작업 가능함
    }
}
