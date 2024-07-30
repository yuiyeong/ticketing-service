package com.yuiyeong.ticketing.presentation.controller.concert

import com.yuiyeong.ticketing.application.annotation.CurrentUserId
import com.yuiyeong.ticketing.application.annotation.RequiresUserToken
import com.yuiyeong.ticketing.application.usecase.concert.GetAvailableSeatsUseCase
import com.yuiyeong.ticketing.application.usecase.occupation.OccupySeatUseCase
import com.yuiyeong.ticketing.application.usecase.reservation.ReserveSeatUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.AvailableSeatsApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.OccupySeatApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.ReserveSeatApiDoc
import com.yuiyeong.ticketing.presentation.dto.TicketingListResponse
import com.yuiyeong.ticketing.presentation.dto.TicketingResponse
import com.yuiyeong.ticketing.presentation.dto.concert.SeatResponseDto
import com.yuiyeong.ticketing.presentation.dto.occupation.ConcertEventOccupationRequest
import com.yuiyeong.ticketing.presentation.dto.occupation.OccupationResponseDto
import com.yuiyeong.ticketing.presentation.dto.reservation.ConcertEventReservationRequest
import com.yuiyeong.ticketing.presentation.dto.reservation.ReservationResponseDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
    @RequiresUserToken
    @AvailableSeatsApiDoc
    fun getAvailableSeats(
        @PathVariable("concertEventId") concertEventId: Long,
    ): TicketingListResponse<SeatResponseDto> {
        val list = getAvailableSeatsUseCase.execute(concertEventId).map { SeatResponseDto.from(it) }
        return TicketingListResponse(list)
    }

    @PostMapping("{concertEventId}/occupy")
    @RequiresUserToken
    @OccupySeatApiDoc
    fun occupy(
        @CurrentUserId userId: Long,
        @PathVariable("concertEventId") concertEventId: Long,
        @RequestBody req: ConcertEventOccupationRequest,
    ): TicketingResponse<OccupationResponseDto> {
        val data = OccupationResponseDto.from(occupySeatUseCase.execute(userId, concertEventId, req.seatId))
        return TicketingResponse(data)
    }

    @PostMapping("{concertEventId}/reserve")
    @RequiresUserToken
    @ReserveSeatApiDoc
    fun reserve(
        @CurrentUserId userId: Long,
        @PathVariable("concertEventId") concertEventId: Long,
        @RequestBody req: ConcertEventReservationRequest,
    ): TicketingResponse<ReservationResponseDto> {
        val data = ReservationResponseDto.from(reserveSeatUseCase.execute(userId, concertEventId, req.seatId))
        return TicketingResponse(data)
    }
}
