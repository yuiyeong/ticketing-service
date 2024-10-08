package com.yuiyeong.ticketing.domain.service.reservation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.OccupationInvalidException
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.exception.ReservationNotFoundException
import com.yuiyeong.ticketing.domain.model.reservation.Reservation
import com.yuiyeong.ticketing.domain.repository.concert.ConcertEventRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.reservation.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val concertEventRepository: ConcertEventRepository,
    private val occupationRepository: OccupationRepository,
) {
    @Transactional
    fun reserve(
        userId: Long,
        concertEventId: Long,
        occupationId: Long,
    ): Reservation {
        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw ConcertEventNotFoundException()
        concertEvent.verifyWithinReservationPeriod(ZonedDateTime.now().asUtc)

        val occupation = occupationRepository.findOneByIdWithLock(occupationId) ?: throw OccupationNotFoundException()
        if (userId != occupation.userId) throw OccupationInvalidException()

        val reservation = reservationRepository.save(Reservation.create(userId, concertEvent, occupation))

        val now = ZonedDateTime.now().asUtc
        occupationRepository.save(occupation.reserve(reservation.id, now))

        return reservation
    }

    @Transactional(readOnly = true)
    fun getReservation(reservationId: Long): Reservation =
        reservationRepository.findOneById(reservationId) ?: throw ReservationNotFoundException()

    @Transactional
    fun confirm(reservationId: Long): Reservation {
        val reservation =
            reservationRepository.findOneByIdWithLock(reservationId) ?: throw ReservationNotFoundException()
        return reservationRepository.save(reservation.confirm())
    }
}
