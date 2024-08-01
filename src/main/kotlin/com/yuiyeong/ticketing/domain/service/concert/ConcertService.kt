package com.yuiyeong.ticketing.domain.service.concert

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.model.concert.Concert
import com.yuiyeong.ticketing.domain.model.concert.ConcertEvent
import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.concert.ConcertRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class ConcertService(
    private val concertRepository: ConcertRepository,
    private val concertEventRepository: ConcertEventRepository,
    private val seatRepository: SeatRepository,
) {
    @Cacheable(value = ["concerts"], key = "'all'")
    @Transactional(readOnly = true)
    fun getConcerts(): List<Concert> = concertRepository.findAll()

    @Cacheable(value = ["availableEvents"], key = "#concertId")
    @Transactional(readOnly = true)
    fun getAvailableEvents(concertId: Long): List<ConcertEvent> {
        val now = ZonedDateTime.now().asUtc
        return concertEventRepository.findAllWithinPeriodBy(concertId, now)
    }

    @Cacheable(value = ["concertEvents"], key = "#concertEventId")
    @Transactional(readOnly = true)
    fun getConcertEvent(concertEventId: Long): ConcertEvent {
        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw ConcertEventNotFoundException()
        return concertEvent
    }

    @Transactional(readOnly = true)
    fun getAvailableSeats(concertEventId: Long): List<Seat> {
        val concertEvent = getConcertEvent(concertEventId)
        concertEvent.verifyWithinReservationPeriod(ZonedDateTime.now().asUtc)

        return seatRepository.findAllAvailableByConcertEventId(concertEvent.id)
    }

    @CachePut(value = ["concertEvents"], key = "#concertEventId")
    @Transactional
    fun refreshAvailableSeats(concertEventId: Long): ConcertEvent {
        val concertEvent =
            concertEventRepository.findOneByIdWithLock(concertEventId) ?: throw ConcertEventNotFoundException()
        val seats = seatRepository.findAllAvailableByConcertEventId(concertEventId)
        return concertEventRepository.save(concertEvent.recalculateAvailableSeatCount(seats))
    }
}
