package com.yuiyeong.ticketing.presentation.controller

import com.yuiyeong.ticketing.config.swagger.annotation.api.PaymentHistoryApiDoc
import com.yuiyeong.ticketing.presentation.dto.PaymentDto
import com.yuiyeong.ticketing.presentation.dto.response.TicketingListResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "결제", description = "결제 관련 api")
class PaymentController {
    @GetMapping("{userId}/payments")
    @PaymentHistoryApiDoc
    fun getPayments(
        @PathVariable("userId") userId: Long,
    ): TicketingListResponse<PaymentDto> =
        TicketingListResponse(
            when (userId) {
                1L ->
                    listOf(
                        PaymentDto(
                            id = 21,
                            reservationId = 12,
                            amount = 60000,
                            status = "failed",
                            paidAt = "2024-07-02T14:20:00Z",
                        ),
                        PaymentDto(
                            id = 22,
                            reservationId = 12,
                            amount = 60000,
                            status = "success",
                            paidAt = "2024-07-02T14:25:00Z",
                        ),
                    )

                else -> listOf()
            },
        )
}
