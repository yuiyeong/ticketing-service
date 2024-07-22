package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.queue.QueueEntryResult

interface ActivateWaitingEntriesUseCase {
    fun execute(): List<QueueEntryResult>
}
