package com.yuiyeong.ticketing.domain.repository

import com.yuiyeong.ticketing.domain.model.Seat

interface SeatRepository {
    fun save(seat: Seat): Seat
}
