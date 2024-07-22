package com.yuiyeong.ticketing.presentation.dto.queue

import com.yuiyeong.ticketing.application.dto.queue.QueueEntryResult

data class QueuePositionResponseDto(
    val queuePosition: Long,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(entryDto: QueueEntryResult): QueuePositionResponseDto =
            QueuePositionResponseDto(entryDto.position, entryDto.estimatedWaitingTime)
    }
}
