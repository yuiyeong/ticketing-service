package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventResult

interface AvailableEventsUseCase {
    fun getConcertEvents(
        userToken: String?,
        concertId: Long,
    ): List<ConcertEventResult>
}
