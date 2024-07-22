package com.yuiyeong.ticketing.domain.service.occupation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class OccupationService(
    @Value("\${config.valid-occupied-duration}") private val validOccupiedDuration: Long,
    private val occupationRepository: OccupationRepository,
    private val seatRepository: SeatRepository,
) {
    fun createOccupation(
        userId: Long,
        concertEventId: Long,
        seatIds: List<Long>,
    ): Occupation {
        val seats = seatRepository.findAllAvailableByIds(seatIds)

        if (seats.count() != seatIds.count()) throw SeatUnavailableException()

        val occupation = Occupation.create(userId, concertEventId, seats, validOccupiedDuration)
        return occupationRepository.save(occupation)
    }

    fun release(
        userId: Long,
        occupationId: Long,
    ): Occupation {
        val occupation =
            occupationRepository.findOneByIdWithLock(occupationId) ?: throw OccupationNotFoundException()
        val now = ZonedDateTime.now().asUtc
        occupation.release(now)
        return occupationRepository.save(occupation)
    }

    fun expireOverdueOccupations(): List<Occupation> {
        val current = ZonedDateTime.now().asUtc
        val occupations = occupationRepository.findAllByExpiresAtBeforeWithLock(current)
        occupations.forEach { it.expire() }
        return occupationRepository.saveAll(occupations)
    }
}
