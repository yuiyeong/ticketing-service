package com.yuiyeong.ticketing.config.swagger.schema.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "결제 내역 정보")
data class PaymentHistoryData(
    @field:Schema(description = "결제 ID", example = "12")
    val id: Long,
    @field:Schema(description = "예약 ID", example = "5679")
    val reservationId: Long,
    @field:Schema(description = "결제 금액", example = "60000")
    val amount: Int,
    @field:Schema(description = "결제 상태", example = "success")
    val status: String,
    @field:Schema(description = "결제 시간", example = "2023-07-02T14:30:00Z")
    val paidAt: String,
)

@Schema(description = "결제 내역 목록 응답")
data class PaymentHistoryResponse(
    @Schema(description = "결제 내역 목록")
    val list: List<PaymentHistoryData>,
)
