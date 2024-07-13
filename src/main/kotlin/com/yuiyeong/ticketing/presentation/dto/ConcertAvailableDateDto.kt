package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.ConcertEventDto
import java.time.ZonedDateTime

data class ConcertAvailableDateDto(
    val id: Long,
    val date: ZonedDateTime,
) {
    companion object {
        fun from(concertEventDto: ConcertEventDto): ConcertAvailableDateDto =
            ConcertAvailableDateDto(concertEventDto.id, concertEventDto.performanceSchedule.start)
    }
}
