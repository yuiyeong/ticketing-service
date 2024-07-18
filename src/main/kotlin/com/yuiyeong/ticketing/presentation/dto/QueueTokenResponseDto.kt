package com.yuiyeong.ticketing.presentation.dto

import com.yuiyeong.ticketing.application.dto.QueueEntryResult

data class QueueTokenResponseDto(
    val token: String,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(entityDto: QueueEntryResult): QueueTokenResponseDto =
            QueueTokenResponseDto(entityDto.token, entityDto.estimatedWaitingTime)
    }
}
