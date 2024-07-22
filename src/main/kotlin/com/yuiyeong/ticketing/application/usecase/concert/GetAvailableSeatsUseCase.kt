package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.SeatResult

interface GetAvailableSeatsUseCase {
    fun execute(concertEventId: Long): List<SeatResult>
}
