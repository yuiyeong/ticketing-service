package com.yuiyeong.ticketing.domain.service.occupation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class OccupationService(
    @Value("\${config.valid-occupied-duration}") private val validOccupiedDuration: Long,
    private val occupationRepository: OccupationRepository,
    private val seatRepository: SeatRepository,
) {
    @Transactional
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

    @Transactional
    fun expireOverdueOccupations(): List<Occupation> {
        val current = ZonedDateTime.now().asUtc
        val occupations = occupationRepository.findAllByStatusAndExpiresAtBeforeWithLock(OccupationStatus.ACTIVE, current)
        return occupationRepository.saveAll(occupations.map { it.expire() })
    }
}
