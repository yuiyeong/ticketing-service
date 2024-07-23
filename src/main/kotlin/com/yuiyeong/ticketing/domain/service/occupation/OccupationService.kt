package com.yuiyeong.ticketing.domain.service.occupation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class OccupationService(
    @Value("\${config.valid-occupied-duration}") private val validOccupiedDuration: Long,
    private val occupationRepository: OccupationRepository,
    private val seatRepository: SeatRepository,
) {
    fun occupy(
        userId: Long,
        concertEventId: Long,
        seatIds: List<Long>,
    ): Occupation {
        if (seatIds.isEmpty()) throw SeatUnavailableException()

        val seats = seatRepository.findAllAvailableByIdsWithLock(seatIds)

        if (seats.count() != seatIds.count()) throw SeatUnavailableException()

        val occupiedSeats = seatRepository.saveAll(seats.map { it.makeUnavailable() })
        val occupation = Occupation.create(userId, concertEventId, occupiedSeats, validOccupiedDuration)
        return occupationRepository.save(occupation)
    }

    fun release(
        userId: Long,
        occupationId: Long,
    ): Occupation {
        val occupation =
            occupationRepository.findOneByIdWithLock(occupationId) ?: throw OccupationNotFoundException()
        val now = ZonedDateTime.now().asUtc
        return occupationRepository.save(occupation.release(now))
    }

    fun expireOverdueOccupations(): List<Occupation> {
        val current = ZonedDateTime.now().asUtc
        val occupations = occupationRepository.findAllByExpiresAtBeforeWithLock(current)
        return occupationRepository.saveAll(occupations.map { it.expire() })
    }
}
