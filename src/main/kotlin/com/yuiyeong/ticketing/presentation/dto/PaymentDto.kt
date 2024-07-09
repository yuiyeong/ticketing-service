package com.yuiyeong.ticketing.presentation.dto

data class PaymentDto(
    val id: Long,
    val reservationId: Long,
    val amount: Int,
    val status: String,
    val paidAt: String,
)
