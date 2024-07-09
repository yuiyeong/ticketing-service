package com.yuiyeong.ticketing.presentation.dto

data class ReservationDto(
    val id: Long,
    val concertEventId: Long,
    val totalSeats: Long,
    val totalAmount: Long,
    val createdAt: String,
)
