package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.usecase.concert.GetAvailableConcertEventsUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.AvailableConcertEventsApiDoc
import com.yuiyeong.ticketing.presentation.dto.ConcertEventResponseDto
import com.yuiyeong.ticketing.presentation.dto.response.TicketingListResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/concerts")
@Tag(name = "콘서트", description = "콘서트 관련 api")
class ConcertController(
    private val getAvailableConcertEventsUseCase: GetAvailableConcertEventsUseCase,
) {
    @GetMapping("{concertId}/available-events")
    @AvailableConcertEventsApiDoc
    fun getAvailableEvents(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertId") concertId: Long,
    ): TicketingListResponse<ConcertEventResponseDto> {
        val list =
            getAvailableConcertEventsUseCase
                .execute(userToken, concertId)
                .map { ConcertEventResponseDto.from(it) }
        return TicketingListResponse(list)
    }
}
