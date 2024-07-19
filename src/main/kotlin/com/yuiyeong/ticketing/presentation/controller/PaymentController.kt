package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.annotation.CurrentEntry
import com.yuiyeong.ticketing.application.annotation.RequiresUserToken
import com.yuiyeong.ticketing.application.dto.QueueEntryResult
import com.yuiyeong.ticketing.application.usecase.payment.GetPaymentListUseCase
import com.yuiyeong.ticketing.application.usecase.payment.PayUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.PayApiDoc
import com.yuiyeong.ticketing.config.swagger.annotation.api.PaymentHistoryApiDoc
import com.yuiyeong.ticketing.presentation.dto.PaymentResponseDto
import com.yuiyeong.ticketing.presentation.dto.request.PayRequest
import com.yuiyeong.ticketing.presentation.dto.response.TicketingListResponse
import com.yuiyeong.ticketing.presentation.dto.response.TicketingResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "결제", description = "결제 관련 api")
class PaymentController(
    private val payUseCase: PayUseCase,
    private val getPaymentListUseCase: GetPaymentListUseCase,
) {
    @PostMapping("payments")
    @RequiresUserToken
    @PayApiDoc
    fun pay(
        @CurrentEntry entry: QueueEntryResult,
        @RequestBody req: PayRequest,
    ): TicketingResponse<PaymentResponseDto> {
        val data = PaymentResponseDto.from(payUseCase.execute(entry.userId, entry.id, req.reservationId))
        return TicketingResponse(data)
    }

    @GetMapping("users/{userId}/payments")
    @PaymentHistoryApiDoc
    fun getPayments(
        @PathVariable("userId") userId: Long,
    ): TicketingListResponse<PaymentResponseDto> {
        val list = getPaymentListUseCase.execute(userId).map { PaymentResponseDto.from(it) }
        return TicketingListResponse(list)
    }
}
