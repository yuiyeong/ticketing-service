package com.yuiyeong.ticketing.domain.repository.concert

import com.yuiyeong.ticketing.domain.model.concert.Seat

interface SeatRepository {
    fun save(seat: Seat): Seat

    fun findAllByIds(ids: List<Long>): List<Seat>

    fun findAllAvailableByIds(ids: List<Long>): List<Seat>

    fun findAllAvailableWithLockByIds(ids: List<Long>): List<Seat>

    fun findAllAvailableByConcertEventId(concertEventId: Long): List<Seat>

    fun saveAll(seats: List<Seat>): List<Seat>

    fun deleteAll()
}
