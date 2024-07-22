package com.yuiyeong.ticketing.application.usecase.token

import com.yuiyeong.ticketing.application.dto.queue.QueueEntryResult

interface ValidateTokenUseCase {
    fun execute(token: String): QueueEntryResult
}
