package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.SeatNotFoundException
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

        val concertEvent = concertEventRepository.findOneById(concertEventId) ?: throw ConcertEventNotFoundException()
        concertEvent.checkReservationPeriod(now)

        val seat = concertEvent.findSeatBySeatId(seatId) ?: throw SeatNotFoundException()
        seat.checkAvailable()

        val occupation = Occupation.create(userId, seat)

        seatRepository.save(seat)
        concertEventRepository.save(concertEvent)
        return occupationRepository.save(occupation)
    }

    fun expireOverdueOccupations(): List<Occupation> {
        val current = ZonedDateTime.now()
        val occupations = occupationRepository.findAllByExpiresAtBefore(current)
        occupations.forEach { it.expire(current) }
        return occupationRepository.saveAll(occupations)
    }

    fun getUserOccupation(
        userId: Long,
        occupiedSeatId: Long,
    ): Occupation {
        val occupation =
            occupationRepository.findOneByIdAndUserId(occupiedSeatId, userId) ?: throw SeatNotFoundException()
        return occupation
    }
}
