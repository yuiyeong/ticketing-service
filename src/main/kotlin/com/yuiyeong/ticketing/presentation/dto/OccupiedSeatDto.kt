package com.yuiyeong.ticketing.presentation.dto

data class OccupiedSeatDto(
    val id: Long,
    val seatNumber: String,
    val price: Int,
    val expirationTime: String,
)
