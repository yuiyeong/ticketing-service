package com.yuiyeong.ticketing.application.usecase.token

import com.yuiyeong.ticketing.application.dto.QueueEntryResult

interface ValidateTokenUseCase {
    fun execute(token: String): QueueEntryResult
}
