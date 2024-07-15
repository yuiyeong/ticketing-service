package com.yuiyeong.ticketing.domain.service

import com.yuiyeong.ticketing.domain.exception.OccupationNotFoundException
import com.yuiyeong.ticketing.domain.model.Occupation
import com.yuiyeong.ticketing.domain.repository.OccupationRepository
import java.time.ZonedDateTime

class OccupationService(
    private val occupationRepository: OccupationRepository,
) {
    fun createOccupation(
        userId: Long,
        seatIds: List<Long>,
    ): Occupation {
        val occupation = Occupation.create(userId, seatIds)
        return occupationRepository.save(occupation)
    }

    fun release(
        userId: Long,
        occupiedSeatIds: List<Long>,
    ): Occupation {
        val occupation =
            occupationRepository.findOneByUserIdAndSeatIds(userId, occupiedSeatIds) ?: throw OccupationNotFoundException()
        val now = ZonedDateTime.now()
        occupation.release(now)
        return occupationRepository.save(occupation)
    }

    fun expireOverdueOccupations(): List<Occupation> {
        val current = ZonedDateTime.now()
        val occupations = occupationRepository.findAllByExpiresAtBefore(current)
        occupations.forEach { it.expire() }
        return occupationRepository.saveAll(occupations)
    }
}
