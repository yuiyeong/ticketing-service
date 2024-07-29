package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.ConcertResult

interface GetConcertsUseCase {
    fun execute(): List<ConcertResult>
}
