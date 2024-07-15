package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.Seat
import com.yuiyeong.ticketing.domain.repository.SeatRepository

class SeatService(
    private val seatRepository: SeatRepository,
) {
    fun getAvailableSeats(concertEventId: Long): List<Seat> = seatRepository.findAllAvailableByConcertEventId(concertEventId)

    fun occupy(seatIds: List<Long>): List<Seat> {
        val seats = seatRepository.findAllAvailableByIds(seatIds)
        if (seats.count() != seatIds.count()) throw SeatUnavailableException()

        seats.forEach { it.makeUnavailable() }
        return seatRepository.saveAll(seats)
    }
}
