package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

data class WaitingPositionResponseDto(
    val queuePosition: Long,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(entryDto: WaitingEntryResult): WaitingPositionResponseDto =
            WaitingPositionResponseDto(entryDto.position, entryDto.estimatedWaitingTime)
    }
}
