package com.yuiyeong.ticketing.application.service

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto
import com.yuiyeong.ticketing.application.usecase.QueueUseCase
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.model.WaitingEntry
import com.yuiyeong.ticketing.domain.model.WaitingEntryStatus
import com.yuiyeong.ticketing.domain.repository.WaitingEntryRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class QueueService(
    private val repository: WaitingEntryRepository,
) : QueueUseCase {
    override fun enter(userId: Long): WaitingEntryDto {
        // 기존에 발급 받은 token 이 있다면, 대기열에 제거
        dequeueExistingEntries(userId)

        val firstPosition = repository.findFirstWaitingPosition() ?: 0
        val lastPosition = repository.findLastWaitingPosition() ?: 0
        val activeSize = repository.findAllByStatus(WaitingEntryStatus.PROCESSING).size
        val status = if (activeSize < MAX_ACTIVE_ENTRIES) WaitingEntryStatus.PROCESSING else WaitingEntryStatus.WAITING
        val newPosition = if (status == WaitingEntryStatus.WAITING) lastPosition + 1 else 0

        val entry = WaitingEntry.create(userId, newPosition, status)
        repository.save(entry)
        return WaitingEntryDto.from(entry, firstPosition)
    }

    override fun exit(token: String): WaitingEntryDto {
        val entry = repository.findOneByToken(token) ?: throw InvalidTokenException()
        entry.exit()
        repository.save(entry)
        return WaitingEntryDto.from(entry)
    }

    override fun getEntryInfo(token: String): WaitingEntryDto {
        val entry = repository.findOneByToken(token) ?: throw InvalidTokenException()
        val firstPosition = repository.findLastWaitingPosition() ?: 0
        return WaitingEntryDto.from(entry, firstPosition)
    }

    override fun activateWaitingEntries(): List<WaitingEntryDto> {
        val alreadyActivatedEntries = repository.findAllByStatus(WaitingEntryStatus.PROCESSING)
        val newActivatingCount = MAX_ACTIVE_ENTRIES - alreadyActivatedEntries.count()
        if (newActivatingCount <= 0) {
            return emptyList() // there is no one to be activated.
        }

        val waitingEntries = repository.findAllByStatusOrderByPosition(WaitingEntryStatus.WAITING, newActivatingCount)
        waitingEntries.forEach {
            it.process()
            repository.save(it)
        }
        return waitingEntries.map { WaitingEntryDto.from(it) }
    }

    override fun expireOverdueEntries(): List<WaitingEntryDto> {
        val current = ZonedDateTime.now()
        val entries = repository.findOverdueEntriesByStatus(WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
        entries.forEach {
            it.expire(current)
            repository.save(it)
        }
        return entries.map { WaitingEntryDto.from(it) }
    }

    /**
     * 대기열에 있는 userId 에 해당하는 entry 를 제거
     */
    private fun dequeueExistingEntries(userId: Long) {
        repository
            .findAllByUserIdWithStatus(userId, WaitingEntryStatus.PROCESSING, WaitingEntryStatus.WAITING)
            .forEach { exit(it.token) }
    }

    companion object {
        const val MAX_ACTIVE_ENTRIES = 10 // 최대 10 명까지 작업 가능함
    }
}
