package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.ConcertEvent
import java.time.ZonedDateTime

interface ConcertEventRepository {
    fun save(concertEvent: ConcertEvent): ConcertEvent

    fun saveAll(concertEvents: List<ConcertEvent>): List<ConcertEvent>

    fun findAllWithinPeriodBy(
        concertId: Long,
        moment: ZonedDateTime,
    ): List<ConcertEvent>

    fun findOneById(id: Long): ConcertEvent?

    fun findOneByIdWithLock(id: Long): ConcertEvent?

    fun deleteAll()
}
