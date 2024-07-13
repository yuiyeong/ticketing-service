package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

interface EnterWaitingQueueUseCase {
    fun execute(userId: Long): WaitingEntryResult
}
