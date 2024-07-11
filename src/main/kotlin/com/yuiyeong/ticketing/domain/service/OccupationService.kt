package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.NotFoundConcertEventException
import com.yuiyeong.ticketing.domain.exception.NotFoundSeatException
import com.yuiyeong.ticketing.domain.model.Occupation
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import java.time.ZonedDateTime

class OccupationService(
    private val concertEventRepository: ConcertEventRepository,
    private val seatRepository: SeatRepository,
    private val occupationRepository: OccupationRepository,
) {
    fun occupySeat(
        userId: Long,
        concertEventId: Long,
        seatId: Long,
    ): Occupation {
        val now = ZonedDateTime.now()

        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw NotFoundConcertEventException()
        concertEvent.checkReservationPeriod(now)

        val seat = concertEvent.findSeatBySeatId(seatId) ?: throw NotFoundSeatException()
        seat.checkAvailable()

        val occupation = Occupation.create(userId, seat)

        seatRepository.save(seat)
        concertEventRepository.save(concertEvent)
        return occupationRepository.save(occupation)
    }
}
