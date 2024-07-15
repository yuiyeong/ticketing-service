package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

interface GetWaitingEntryUseCase {
    fun execute(token: String?): WaitingEntryResult
}
