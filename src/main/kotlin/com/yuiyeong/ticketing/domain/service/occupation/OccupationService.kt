package com.yuiyeong.ticketing.domain.service.occupation

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.model.occupation.Occupation
import com.yuiyeong.ticketing.domain.model.occupation.OccupationStatus
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.domain.repository.occupation.OccupationRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class OccupationService(
    @Value("\${config.valid-occupied-duration}") private val validOccupiedDuration: Long,
    private val occupationRepository: OccupationRepository,
    private val seatRepository: SeatRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun occupy(
        userId: Long,
        concertEventId: Long,
        seatIds: List<Long>,
    ): Occupation {
        if (seatIds.isEmpty()) throw SeatUnavailableException()

        val seats = seatRepository.findAllAvailableWithLockByIds(seatIds)

        // 점유 요청한 좌석 중 점유 가능하지 않은 상태(이미 얘약이 되었거나 다른 사람이 점유 중)가 있는지 검증
        if (seats.count() != seatIds.count()) throw SeatUnavailableException()

        val occupiedSeats = seatRepository.saveAll(seats.map { it.makeUnavailable() })
        val occupation = Occupation.create(userId, concertEventId, occupiedSeats, validOccupiedDuration)
        return occupationRepository.save(occupation)
    }

    @Transactional
    fun expireOverdueOccupations(): List<Occupation> {
        val current = ZonedDateTime.now().asUtc
        val occupations =
            occupationRepository.findAllByStatusAndExpiresAtBeforeWithLock(OccupationStatus.ACTIVE, current)
        return occupationRepository.saveAll(occupations.map { it.expire() })
    }
}
