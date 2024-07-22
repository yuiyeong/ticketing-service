package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.ConcertEventResult

interface GetAvailableConcertEventsUseCase {
    fun execute(concertId: Long): List<ConcertEventResult>
}
