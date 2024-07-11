package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

data class WaitingInfoTokenDto(
    val token: String,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(entityDto: WaitingEntryDto): WaitingInfoTokenDto = WaitingInfoTokenDto(entityDto.token, entityDto.estimatedWaitingTime)
    }
}
