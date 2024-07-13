package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.ConcertEventResult

interface GetAvailableConcertEventsUseCase {
    fun execute(
        userToken: String?,
        concertId: Long,
    ): List<ConcertEventResult>
}
