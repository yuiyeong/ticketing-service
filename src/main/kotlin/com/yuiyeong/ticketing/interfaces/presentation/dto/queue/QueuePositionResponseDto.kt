package com.yuiyeong.ticketing.interfaces.presentation.dto.queue

import com.yuiyeong.ticketing.application.dto.queue.WaitingInfoResult

data class QueuePositionResponseDto(
    val position: Int,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(result: WaitingInfoResult): QueuePositionResponseDto =
            QueuePositionResponseDto(
                position = result.position,
                estimatedWaitingTime = result.estimatedWaitingTime,
            )
    }
}
