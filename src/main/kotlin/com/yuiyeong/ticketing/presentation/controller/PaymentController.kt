package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.application.usecase.payment.GetPaymentListUseCase
import com.yuiyeong.ticketing.config.swagger.annotation.api.PaymentHistoryApiDoc
import com.yuiyeong.ticketing.presentation.dto.PaymentResponseDto
import com.yuiyeong.ticketing.presentation.dto.response.TicketingListResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "결제", description = "결제 관련 api")
class PaymentController {
    @Autowired
    private lateinit var getPaymentListUseCase: GetPaymentListUseCase

    @GetMapping("{userId}/payments")
    @PaymentHistoryApiDoc
    fun getPayments(
        @PathVariable("userId") userId: Long,
    ): TicketingListResponse<PaymentResponseDto> {
        val list = getPaymentListUseCase.execute(userId).map { PaymentResponseDto.from(it) }
        return TicketingListResponse(list)
    }
}
