package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class ConcertEventService(
    private val concertEventRepository: ConcertEventRepository,
    private val seatRepository: SeatRepository,
) {
    fun getAvailableEvents(concertId: Long): List<ConcertEvent> {
        val now = ZonedDateTime.now().asUtc
        return concertEventRepository.findAllWithinPeriodBy(concertId, now)
    }

    fun getConcertEvent(concertEventId: Long): ConcertEvent {
        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw ConcertEventNotFoundException()
        return concertEvent
    }

    fun refreshAvailableSeats(concertEventId: Long) {
        val concertEvent = concertEventRepository.findOneByIdWithLock(concertEventId) ?: throw ConcertEventNotFoundException()
        val seats = seatRepository.findAllAvailableByConcertEventId(concertEventId)
        concertEvent.recalculateAvailableSeatCount(seats)
        concertEventRepository.save(concertEvent)
    }
}
