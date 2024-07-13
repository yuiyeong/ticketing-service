package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventDto

interface AvailableEventsUseCase {
    fun getConcertEvents(
        userToken: String?,
        concertId: Long,
    ): List<ConcertEventDto>
}
