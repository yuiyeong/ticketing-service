package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.QueueEntryResult

interface EnterQueueUseCase {
    fun execute(userId: Long): QueueEntryResult
}
