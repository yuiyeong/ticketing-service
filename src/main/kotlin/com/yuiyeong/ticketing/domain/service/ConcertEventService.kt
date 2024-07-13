package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.ConcertNotFoundException
import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.model.Seat
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import java.time.ZonedDateTime

class ConcertEventService(
    private val concertEventRepository: ConcertEventRepository,
) {
    fun getAvailableEvents(concertId: Long): List<ConcertEvent> {
        val now = ZonedDateTime.now()
        return concertEventRepository.findAllWithinPeriodBy(concertId, now)
    }

    fun getAvailableSeats(concertEventId: Long): List<Seat> {
        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw ConcertNotFoundException()
        return concertEvent.getAvailableSeats()
    }

    fun getConcertEvent(concertEventId: Long): ConcertEvent {
        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw ConcertEventNotFoundException()
        return concertEvent
    }
}
