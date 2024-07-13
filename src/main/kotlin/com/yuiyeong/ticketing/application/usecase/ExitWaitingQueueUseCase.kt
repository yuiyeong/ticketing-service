package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

interface ExitWaitingQueueUseCase {
    fun execute(token: String): WaitingEntryResult
}
