package com.yuiyeong.ticketing.interfaces.api.dto.concert

import com.yuiyeong.ticketing.application.dto.concert.ConcertEventResult
import java.time.ZonedDateTime

data class ConcertEventResponseDto(
    val id: Long,
    val date: ZonedDateTime,
) {
    companion object {
        fun from(concertEventResult: ConcertEventResult): ConcertEventResponseDto =
            ConcertEventResponseDto(concertEventResult.id, concertEventResult.performanceSchedule.start)
    }
}
