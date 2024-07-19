package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.QueueEntryResult

interface ExpireWaitingEntryUseCase {
    fun execute(): List<QueueEntryResult>
}
