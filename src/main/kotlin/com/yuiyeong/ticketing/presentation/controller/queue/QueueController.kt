package com.yuiyeong.ticketing.presentation.controller.queue

import com.yuiyeong.ticketing.application.annotation.CurrentToken
import com.yuiyeong.ticketing.application.annotation.RequiresUserToken
import com.yuiyeong.ticketing.application.usecase.queue.EnterQueueUseCase
import com.yuiyeong.ticketing.application.usecase.queue.GetWaitingInfoUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.QueueStatusApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.QueueTokenIssuanceApiDoc
import com.yuiyeong.ticketing.presentation.dto.TicketingResponse
import com.yuiyeong.ticketing.presentation.dto.queue.GeneratingQueueTokenRequest
import com.yuiyeong.ticketing.presentation.dto.queue.QueuePositionResponseDto
import com.yuiyeong.ticketing.presentation.dto.queue.QueueTokenResponseDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/queue")
@Tag(name = "대기열", description = "대기열 관련 API")
class QueueController(
    private val enterQueueUseCase: EnterQueueUseCase,
    private val getWaitingInfoUseCase: GetWaitingInfoUseCase,
) {
    @PostMapping("token")
    @QueueTokenIssuanceApiDoc
    fun generateToken(
        @RequestBody req: GeneratingQueueTokenRequest,
    ): TicketingResponse<QueueTokenResponseDto> {
        val data = QueueTokenResponseDto.from(enterQueueUseCase.execute(req.userId))
        return TicketingResponse(data)
    }

    @GetMapping("status")
    @RequiresUserToken(onlyProcessing = false)
    @QueueStatusApiDoc
    fun getStatus(
        @CurrentToken token: String,
    ): TicketingResponse<QueuePositionResponseDto> {
        val data = QueuePositionResponseDto.from(getWaitingInfoUseCase.execute(token))
        return TicketingResponse(data)
    }
}
