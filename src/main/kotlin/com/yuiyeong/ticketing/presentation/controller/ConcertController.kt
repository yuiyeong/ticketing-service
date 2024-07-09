package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.NotFoundConcertException
import com.yuiyeong.ticketing.domain.exception.NotFoundTokenException
import com.yuiyeong.ticketing.presentation.dto.ConcertAvailableDateDto
import com.yuiyeong.ticketing.presentation.dto.response.TicketingListResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/concerts")
class ConcertController {
    @GetMapping("{concertId}/available-events")
    fun getAvailableDates(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertId") concertId: Long,
    ): TicketingListResponse<ConcertAvailableDateDto> {
        when (userToken) {
            null -> throw InvalidTokenException()
            "invalidQueueToken" -> throw InvalidTokenException()
            "notInQueueToken" -> throw NotFoundTokenException()
        }
        return TicketingListResponse(
            when (concertId) {
                1L -> listOf(ConcertAvailableDateDto(1, "2024-07-07"))
                2L -> listOf()
                else -> throw NotFoundConcertException()
            },
        )
    }
}
