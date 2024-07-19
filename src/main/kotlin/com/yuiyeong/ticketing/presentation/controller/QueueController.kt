package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.annotation.CurrentEntry
import com.yuiyeong.ticketing.application.annotation.RequiresUserToken
import com.yuiyeong.ticketing.application.dto.QueueEntryResult
import com.yuiyeong.ticketing.application.usecase.queue.EnterQueueUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.QueueStatusApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.QueueTokenIssuanceApiDoc
import com.yuiyeong.ticketing.domain.model.QueueEntryStatus
import com.yuiyeong.ticketing.presentation.dto.QueuePositionResponseDto
import com.yuiyeong.ticketing.presentation.dto.QueueTokenResponseDto
import com.yuiyeong.ticketing.presentation.dto.request.GeneratingQueueTokenRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
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
    @RequiresUserToken(
        allowedStatus = [
            QueueEntryStatus.WAITING,
            QueueEntryStatus.PROCESSING,
            QueueEntryStatus.EXITED,
            QueueEntryStatus.EXPIRED,
        ],
    )
    @QueueStatusApiDoc
    fun getStatus(
        @CurrentEntry entry: QueueEntryResult,
    ): TicketingResponse<QueuePositionResponseDto> {
        val data = QueuePositionResponseDto.from(entry)
        return TicketingResponse(data)
    }
}
