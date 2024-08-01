package com.yuiyeong.ticketing.presentation.dto.queue

import com.yuiyeong.ticketing.application.dto.queue.WaitingInfoResult

data class QueueTokenResponseDto(
    val token: String,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(result: WaitingInfoResult): QueueTokenResponseDto =
            QueueTokenResponseDto(
                token = result.token,
                estimatedWaitingTime = result.estimatedWaitingTime,
            )
    }
}
