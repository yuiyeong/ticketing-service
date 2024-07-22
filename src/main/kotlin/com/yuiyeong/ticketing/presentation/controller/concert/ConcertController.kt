package com.yuiyeong.ticketing.presentation.controller.concert

import com.yuiyeong.ticketing.application.annotation.RequiresUserToken
import com.yuiyeong.ticketing.application.usecase.concert.GetAvailableConcertEventsUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.AvailableConcertEventsApiDoc
import com.yuiyeong.ticketing.presentation.dto.TicketingListResponse
import com.yuiyeong.ticketing.presentation.dto.concert.ConcertEventResponseDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/concerts")
@Tag(name = "콘서트", description = "콘서트 관련 api")
class ConcertController(
    private val getAvailableConcertEventsUseCase: GetAvailableConcertEventsUseCase,
) {
    @GetMapping("{concertId}/available-events")
    @RequiresUserToken
    @AvailableConcertEventsApiDoc
    fun getAvailableEvents(
        @PathVariable("concertId") concertId: Long,
    ): TicketingListResponse<ConcertEventResponseDto> {
        val list = getAvailableConcertEventsUseCase.execute(concertId).map { ConcertEventResponseDto.from(it) }
        return TicketingListResponse(list)
    }
}
