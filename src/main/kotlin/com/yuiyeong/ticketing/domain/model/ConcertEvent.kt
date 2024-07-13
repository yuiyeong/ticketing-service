package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.OutOfPeriodException
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
import java.time.ZonedDateTime

data class ConcertEvent(
    val id: Long,
    val concertId: Long,
    val venue: String,
    val reservationPeriod: DateTimeRange,
    val performanceSchedule: DateTimeRange,
    val seats: List<Seat>,
) {
    fun getAvailableSeats(): List<Seat> = seats.filter { it.isAvailable }

    fun findSeatBySeatId(seatId: Long): Seat? = seats.firstOrNull { it.id == seatId }

    fun checkReservationPeriod(moment: ZonedDateTime) {
        if (!reservationPeriod.contains(moment)) {
            throw OutOfPeriodException()
        }
    }
}
