package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.WaitingEntry
import com.yuiyeong.ticketing.domain.model.WaitingEntryStatus
import java.time.ZonedDateTime

interface WaitingEntryRepository {
    fun save(entry: WaitingEntry): WaitingEntry

    fun saveAll(entries: List<WaitingEntry>): List<WaitingEntry>

    fun findOneByToken(token: String): WaitingEntry?

    fun findOneById(id: Long): WaitingEntry?

    fun findAllByUserIdWithStatus(
        userId: Long,
        vararg status: WaitingEntryStatus,
    ): List<WaitingEntry>

    fun findAllByStatus(vararg status: WaitingEntryStatus): List<WaitingEntry>

    fun findAllByStatusOrderByPosition(
        status: WaitingEntryStatus,
        limit: Int,
    ): List<WaitingEntry>

    fun updateStatus(
        id: Long,
        status: WaitingEntryStatus,
    ): WaitingEntry

    fun findAllByExpiresAtBeforeAndStatus(
        moment: ZonedDateTime,
        vararg status: WaitingEntryStatus,
    ): List<WaitingEntry>

    fun findLastWaitingPosition(): Long?

    fun findFirstWaitingPosition(): Long?
}
