package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

interface ActivateWaitingEntriesUseCase {
    fun execute(): List<WaitingEntryResult>
}
