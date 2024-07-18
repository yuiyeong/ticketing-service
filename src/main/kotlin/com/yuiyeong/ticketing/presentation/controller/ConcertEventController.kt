package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.dto.QueueEntryResult
import com.yuiyeong.ticketing.application.usecase.concert.GetAvailableSeatsUseCase
import com.yuiyeong.ticketing.application.usecase.reservation.OccupySeatUseCase
import com.yuiyeong.ticketing.application.usecase.reservation.ReserveSeatUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.AvailableSeatsApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.OccupySeatApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.ReserveSeatApiDoc
import com.yuiyeong.ticketing.presentation.dto.OccupationResponseDto
import com.yuiyeong.ticketing.presentation.dto.ReservationResponseDto
import com.yuiyeong.ticketing.presentation.dto.SeatResponseDto
import com.yuiyeong.ticketing.presentation.dto.request.ConcertEventOccupationRequest
import com.yuiyeong.ticketing.presentation.dto.request.ConcertEventReservationRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingListResponse
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
import io.swagger.v3.oas.annotations.tags.Tag
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
class ConcertEventController(
    private val getAvailableSeatsUseCase: GetAvailableSeatsUseCase,
    private val occupySeatUseCase: OccupySeatUseCase,
    private val reserveSeatUseCase: ReserveSeatUseCase,
) {
    @GetMapping("{concertEventId}/available-seats")
    @AvailableSeatsApiDoc
    fun getAvailableSeats(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertEventId") concertEventId: Long,
    ): TicketingListResponse<SeatResponseDto> {
        val list = getAvailableSeatsUseCase.execute(userToken, concertEventId).map { SeatResponseDto.from(it) }
        return TicketingListResponse(list)
    }

    @PostMapping("{concertEventId}/occupy")
    @OccupySeatApiDoc
    fun occupy(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertEventId") concertEventId: Long,
        @RequestBody req: ConcertEventOccupationRequest,
    ): TicketingResponse<OccupationResponseDto> {
        val data = OccupationResponseDto.from(occupySeatUseCase.execute(userToken, concertEventId, req.seatId))
        return TicketingResponse(data)
    }

    @PostMapping("{concertEventId}/reserve")
    @ReserveSeatApiDoc
    fun reserve(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
        @PathVariable("concertEventId") concertEventId: Long,
        @RequestBody req: ConcertEventReservationRequest,
    ): TicketingResponse<ReservationResponseDto> {
        val data = ReservationResponseDto.from(reserveSeatUseCase.execute(userToken, concertEventId, req.seatId))
        return TicketingResponse(data)
    }
}
