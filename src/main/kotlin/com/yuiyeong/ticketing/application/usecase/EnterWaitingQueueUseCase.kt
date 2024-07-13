package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

interface EnterWaitingQueueUseCase {
    fun execute(userId: Long): WaitingEntryResult
}
