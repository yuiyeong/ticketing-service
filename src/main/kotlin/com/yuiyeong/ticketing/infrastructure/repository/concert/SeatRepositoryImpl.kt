package com.yuiyeong.ticketing.infrastructure.repository.concert

import com.yuiyeong.ticketing.domain.model.concert.Seat
import com.yuiyeong.ticketing.domain.repository.concert.SeatRepository
import com.yuiyeong.ticketing.infrastructure.entity.concert.SeatEntity
import org.springframework.stereotype.Repository

@Repository
class SeatRepositoryImpl(
    private val seatJpaRepository: SeatJpaRepository,
) : SeatRepository {
    override fun save(seat: Seat): Seat {
        val seatEntity = seatJpaRepository.save(SeatEntity.from(seat))
        return seatEntity.toSeat()
    }

    override fun findAllByIds(ids: List<Long>): List<Seat> {
        val seatEntities = seatJpaRepository.findAllByIds(ids)
        return seatEntities.map { it.toSeat() }
    }

    override fun findAllAvailableByIds(ids: List<Long>): List<Seat> {
        val seatEntities = seatJpaRepository.findAllAvailableByIds(ids)
        return seatEntities.map { it.toSeat() }
    }

    override fun findAllAvailableWithLockByIds(ids: List<Long>): List<Seat> {
        val seatEntities = seatJpaRepository.findAllAvailableWithLockByIds(ids)
        return seatEntities.map { it.toSeat() }
    }

    override fun findAllAvailableByConcertEventId(concertEventId: Long): List<Seat> {
        val seatEntities = seatJpaRepository.findAllAvailableByConcertEventId(concertEventId)
        return seatEntities.map { it.toSeat() }
    }

    override fun saveAll(seats: List<Seat>): List<Seat> {
        val seatEntities = seatJpaRepository.saveAll(seats.map { SeatEntity.from(it) })
        return seatEntities.map { it.toSeat() }
    }

    override fun deleteAll() = seatJpaRepository.deleteAll()
}
