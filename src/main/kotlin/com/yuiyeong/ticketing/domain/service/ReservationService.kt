package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.model.Occupation
import com.yuiyeong.ticketing.domain.model.Reservation
import com.yuiyeong.ticketing.domain.repository.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.ReservationRepository
import java.time.ZonedDateTime

class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val occupationRepository: OccupationRepository,
) {
    fun reserve(
        userId: Long,
        concertEvent: ConcertEvent,
        occupation: Occupation,
    ): Reservation {
        val now = ZonedDateTime.now()
        concertEvent.checkReservationPeriod(now)
        occupation.checkAvailable()
        val reservation = Reservation.create(userId, concertEvent, occupation)

        occupation.release()
        occupationRepository.save(occupation)

        return reservationRepository.save(reservation)
    }
}
