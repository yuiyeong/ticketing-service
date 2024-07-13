package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.usecase.EnteringQueueUseCase
import com.yuiyeong.ticketing.application.usecase.QueueEntryInfoUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.QueueStatusApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.QueueTokenIssuanceApiDoc
import com.yuiyeong.ticketing.presentation.dto.WaitingPositionResponseDto
import com.yuiyeong.ticketing.presentation.dto.WaitingTokenResponseDto
import com.yuiyeong.ticketing.presentation.dto.request.GeneratingQueueTokenRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/queue")
@Tag(name = "대기열", description = "대기열 관련 API")
class QueueController {
    @Autowired
    private lateinit var enteringQueueUseCase: EnteringQueueUseCase

    @Autowired
    private lateinit var queueEntryInfoUseCase: QueueEntryInfoUseCase

    @PostMapping("token")
    @QueueTokenIssuanceApiDoc
    fun generateToken(
        @RequestBody req: GeneratingQueueTokenRequest,
    ): TicketingResponse<WaitingTokenResponseDto> {
        val data = WaitingTokenResponseDto.from(enteringQueueUseCase.enter(req.userId))
        return TicketingResponse(data)
    }

    @GetMapping("status")
    @QueueStatusApiDoc
    fun getStatus(
        @RequestHeader(name = "User-Token", required = false) userToken: String?,
    ): TicketingResponse<WaitingPositionResponseDto> {
        val data = WaitingPositionResponseDto.from(queueEntryInfoUseCase.getEntry(userToken))
        return TicketingResponse(data)
    }
}
