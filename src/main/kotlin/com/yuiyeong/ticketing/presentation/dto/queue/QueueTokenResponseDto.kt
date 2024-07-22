package com.yuiyeong.ticketing.presentation.dto.queue

import com.yuiyeong.ticketing.application.dto.queue.QueueEntryResult

data class QueueTokenResponseDto(
    val token: String,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(entityDto: QueueEntryResult): QueueTokenResponseDto =
            QueueTokenResponseDto(entityDto.token, entityDto.estimatedWaitingTime)
    }
}
