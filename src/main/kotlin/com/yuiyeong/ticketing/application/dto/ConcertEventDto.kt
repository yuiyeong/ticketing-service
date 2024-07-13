package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.ConcertEvent
import com.yuiyeong.ticketing.domain.vo.DateTimeRange

data class ConcertEventDto(
    val id: Long,
    val venue: String,
    val reservationPeriod: DateTimeRange,
    val performanceSchedule: DateTimeRange,
) {
    companion object {
        fun from(concertEvent: ConcertEvent): ConcertEventDto =
            ConcertEventDto(
                concertEvent.id,
                concertEvent.venue,
                concertEvent.reservationPeriod,
                concertEvent.performanceSchedule,
            )
    }
}
