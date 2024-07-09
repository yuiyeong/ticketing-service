package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.NotFoundTokenException
import com.yuiyeong.ticketing.presentation.dto.WaitingInfoPositionDto
import com.yuiyeong.ticketing.presentation.dto.WaitingInfoTokenDto
import com.yuiyeong.ticketing.presentation.dto.request.GeneratingQueueTokenRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/queue")
class QueueController {
    @PostMapping("token")
    fun generateToken(
        @RequestBody req: GeneratingQueueTokenRequest,
    ): TicketingResponse<WaitingInfoTokenDto> = TicketingResponse(WaitingInfoTokenDto("validQueueToken", 10))

    @GetMapping("status")
    fun getStatus(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
    ): TicketingResponse<WaitingInfoPositionDto> =
        when (userToken) {
            null -> throw InvalidTokenException()
            "invalidQueueToken" -> throw InvalidTokenException()
            "notInQueueToken" -> throw NotFoundTokenException()
            else -> TicketingResponse(WaitingInfoPositionDto(2, 20))
        }
}