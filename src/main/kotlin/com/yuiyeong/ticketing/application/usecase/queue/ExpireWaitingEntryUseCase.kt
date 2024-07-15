package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

interface ExpireWaitingEntryUseCase {
    fun execute(): List<WaitingEntryResult>
}
