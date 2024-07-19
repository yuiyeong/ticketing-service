package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.vo.DateTimeRange

data class ConcertEventResult(
    val id: Long,
    val venue: String,
    val reservationPeriod: DateTimeRange,
    val performanceSchedule: DateTimeRange,
) {
    companion object {
        fun from(concertEvent: ConcertEvent): ConcertEventResult =
            ConcertEventResult(
                concertEvent.id,
                concertEvent.venue,
                concertEvent.reservationPeriod,
                concertEvent.performanceSchedule,
            )
    }
}
