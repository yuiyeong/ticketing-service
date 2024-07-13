package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.usecase.AvailableSeatsUseCase
import com.yuiyeong.ticketing.application.usecase.OccupationUseCase
import com.yuiyeong.ticketing.application.usecase.ReservationUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.AvailableSeatsApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.OccupySeatApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.ReserveSeatApiDoc
import com.yuiyeong.ticketing.presentation.dto.OccupiedSeatDto
import com.yuiyeong.ticketing.presentation.dto.SeatDto
import com.yuiyeong.ticketing.presentation.dto.SimpleReservationDto
import com.yuiyeong.ticketing.presentation.dto.request.ConcertEventOccupationRequest
import com.yuiyeong.ticketing.presentation.dto.request.ConcertEventReservationRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingListResponse
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/concert-events")
@Tag(name = "콘서트 이벤트", description = "콘서트 이벤트 관련 api")
class ConcertEventController {
    @Autowired
    private lateinit var availableSeatsUseCase: AvailableSeatsUseCase

    @Autowired
    private lateinit var occupationUseCase: OccupationUseCase

    @Autowired
    private lateinit var reservationUseCase: ReservationUseCase

    @GetMapping("{concertEventId}/available-seats")
    @AvailableSeatsApiDoc
    fun getAvailableSeats(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertEventId") concertEventId: Long,
    ): TicketingListResponse<SeatDto> {
        val list = availableSeatsUseCase.getSeats(userToken, concertEventId).map { SeatDto.from(it) }
        return TicketingListResponse(list)
    }

    @PostMapping("{concertEventId}/occupy")
    @OccupySeatApiDoc
    fun occupy(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertEventId") concertEventId: Long,
        @RequestBody req: ConcertEventOccupationRequest,
    ): TicketingResponse<OccupiedSeatDto> {
        val data = OccupiedSeatDto.from(occupationUseCase.occupySeat(userToken, concertEventId, req.seatId))
        return TicketingResponse(data)
    }

    @PostMapping("{concertEventId}/reserve")
    @ReserveSeatApiDoc
    fun reserve(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertEventId") concertEventId: Long,
        @RequestBody req: ConcertEventReservationRequest,
    ): TicketingResponse<SimpleReservationDto> {
        val data = SimpleReservationDto.from(reservationUseCase.reserve(userToken, concertEventId, req.seatId))
        return TicketingResponse(data)
    }
}
