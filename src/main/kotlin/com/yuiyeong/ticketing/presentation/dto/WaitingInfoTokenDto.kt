package com.yuiyeong.ticketing.presentation.dto

data class WaitingInfoTokenDto(
    val token: String,
    val estimatedWaitingTime: Int,
)
