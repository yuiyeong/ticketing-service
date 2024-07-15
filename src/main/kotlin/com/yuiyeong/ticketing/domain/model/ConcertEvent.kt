package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.ReservationNotOpenedException
import com.yuiyeong.ticketing.domain.vo.DateTimeRange
import java.time.ZonedDateTime

data class ConcertEvent(
    val id: Long,
    val concert: Concert,
    val venue: String,
    val reservationPeriod: DateTimeRange,
    val performanceSchedule: DateTimeRange,
    val maxSeatCount: Int,
    val availableSeatCount: Int,
) {
    fun verifyWithinReservationPeriod(moment: ZonedDateTime) {
        if (!reservationPeriod.contains(moment)) {
            throw ReservationNotOpenedException()
        }
    }
}
