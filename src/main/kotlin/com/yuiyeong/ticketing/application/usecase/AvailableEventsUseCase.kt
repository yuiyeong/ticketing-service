package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventDto

interface AvailableEventsUseCase {
    fun getConcertEvents(concertId: Long): List<ConcertEventDto>
}
