package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.ConcertEventResult

interface GetAvailableConcertEventsUseCase {
    fun execute(
        userToken: String?,
        concertId: Long,
    ): List<ConcertEventResult>
}
