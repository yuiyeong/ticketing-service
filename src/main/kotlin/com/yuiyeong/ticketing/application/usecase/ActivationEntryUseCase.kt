package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryResult

interface ActivationEntryUseCase {
    fun activateEntries(): List<WaitingEntryResult>
}
