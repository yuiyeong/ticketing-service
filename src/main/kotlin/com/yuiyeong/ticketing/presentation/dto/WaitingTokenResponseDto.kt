package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

data class WaitingTokenResponseDto(
    val token: String,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(entityDto: WaitingEntryResult): WaitingTokenResponseDto =
            WaitingTokenResponseDto(entityDto.token, entityDto.estimatedWaitingTime)
    }
}
