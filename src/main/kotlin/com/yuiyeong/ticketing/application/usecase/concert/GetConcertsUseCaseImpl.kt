package com.yuiyeong.ticketing.application.usecase.concert

import com.yuiyeong.ticketing.application.dto.concert.ConcertResult
import com.yuiyeong.ticketing.domain.service.concert.ConcertService
import org.springframework.stereotype.Component

@Component
class GetConcertsUseCaseImpl(
    private val concertService: ConcertService,
) : GetConcertsUseCase {
    override fun execute(): List<ConcertResult> = concertService.getConcerts().map { ConcertResult.from(it) }
}
