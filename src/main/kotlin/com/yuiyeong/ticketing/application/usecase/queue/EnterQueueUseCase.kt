package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.queue.WaitingInfoResult

interface EnterQueueUseCase {
    fun execute(userId: Long): WaitingInfoResult
}
