package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.domain.exception.InsufficientBalanceException
import com.yuiyeong.ticketing.domain.exception.InvalidSeatStatusException
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.NotFoundTokenException
import com.yuiyeong.ticketing.domain.exception.OccupationExpiredException
import com.yuiyeong.ticketing.presentation.dto.ReservationDto
import com.yuiyeong.ticketing.presentation.dto.request.ConcertEventReservationRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/concert-events")
class ConcertEventController {
    @PostMapping("{concertEventId}/reserve")
    fun reserve(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertEventId") concertEventId: Long,
        @RequestBody req: ConcertEventReservationRequest,
    ): TicketingResponse<ReservationDto> {
        when (userToken) {
            null -> throw InvalidTokenException()
            "invalidQueueToken" -> throw InvalidTokenException()
            "notInQueueToken" -> throw NotFoundTokenException()
        }

        return TicketingResponse(
            when (req.seatId) {
                1234L ->
                    ReservationDto(
                        id = 1L,
                        concertEventId = concertEventId,
                        totalSeats = 1,
                        totalAmount = 50000,
                        createdAt = "2024-07-01T12:05:00Z",
                    )

                9999L -> throw InsufficientBalanceException()
                1111L -> throw OccupationExpiredException()
                else -> throw InvalidSeatStatusException()
            },
        )
    }
}
