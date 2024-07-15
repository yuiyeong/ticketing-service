package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Seat

interface SeatRepository {
    fun save(seat: Seat): Seat

    fun findAllByIds(ids: List<Long>): List<Seat>

    fun findAllAvailableByIds(ids: List<Long>): List<Seat>

    fun findAllAvailableByConcertEventId(concertEventId: Long): List<Seat>

    fun saveAll(seats: List<Seat>): List<Seat>
}
