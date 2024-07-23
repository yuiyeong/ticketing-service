package com.yuiyeong.ticketing.domain.service.concert

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class ConcertService(
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

    fun getAvailableSeats(concertEventId: Long): List<Seat> = seatRepository.findAllAvailableByConcertEventId(concertEventId)

    fun refreshAvailableSeats(concertEventId: Long): ConcertEvent {
        val concertEvent =
            concertEventRepository.findOneByIdWithLock(concertEventId) ?: throw ConcertEventNotFoundException()
        val seats = seatRepository.findAllAvailableByConcertEventId(concertEventId)
        return concertEventRepository.save(concertEvent.recalculateAvailableSeatCount(seats))
    }
}
