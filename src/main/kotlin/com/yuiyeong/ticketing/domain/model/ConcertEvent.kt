package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.vo.DateTimeRange

data class ConcertEvent(
    val id: Long,
    val concertId: Long,
    val venue: String,
    val reservationPeriod: DateTimeRange,
    val performanceSchedule: DateTimeRange,
    val seats: List<Seat>,
) {
    fun getAvailableSeats(): List<Seat> = seats.filter { it.isAvailable }
}
