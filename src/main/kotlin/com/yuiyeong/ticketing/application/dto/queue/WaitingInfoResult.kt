package com.yuiyeong.ticketing.application.dto.queue

import com.yuiyeong.ticketing.domain.model.queue.WaitingInfo

data class WaitingInfoResult(
    val position: Int,
    val token: String,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(waitingInfo: WaitingInfo): WaitingInfoResult =
            WaitingInfoResult(
                position = waitingInfo.position,
                token = waitingInfo.token,
                estimatedWaitingTime = waitingInfo.estimatedWaitingTime,
            )
    }
}
