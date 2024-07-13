package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

data class WaitingInfoPositionDto(
    val queuePosition: Long,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(entryDto: WaitingEntryDto): WaitingInfoPositionDto =
            WaitingInfoPositionDto(entryDto.position, entryDto.estimatedWaitingTime)
    }
}
