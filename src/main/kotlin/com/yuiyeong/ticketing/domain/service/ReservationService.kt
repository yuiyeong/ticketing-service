package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.exception.SeatNotFoundException
import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.repository.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.ReservationRepository
import com.yuiyeong.ticketing.domain.repository.SeatRepository
import java.time.ZonedDateTime

class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val concertEventRepository: ConcertEventRepository,
    private val seatRepository: SeatRepository,
) {
    fun reserve(
        userId: Long,
        concertEventId: Long,
        occupiedSeatIds: List<Long>,
    ): Reservation {
        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw ConcertEventNotFoundException()
        concertEvent.verifyWithinReservationPeriod(ZonedDateTime.now())

        val occupiedSeats = seatRepository.findAllByIds(occupiedSeatIds)
        if (occupiedSeats.count() != occupiedSeatIds.count()) throw SeatNotFoundException()

        val reservation = Reservation.create(userId, concertEvent, occupiedSeats)
        return reservationRepository.save(reservation)
    }

    fun getReservation(reservationId: Long): Reservation {
        val reservation = reservationRepository.findOneById(reservationId) ?: throw ReservationNotFoundException()
        return reservation
    }

    fun confirm(reservationId: Long): Reservation {
        val reservation = getReservation(reservationId).apply { confirm() }
        return reservationRepository.save(reservation)
    }
}
