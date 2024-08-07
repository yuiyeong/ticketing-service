package com.yuiyeong.ticketing.domain.model.queue

data class WaitingInfo(
    val token: String,
    val position: Int,
    val estimatedWaitingTime: Long,
)
