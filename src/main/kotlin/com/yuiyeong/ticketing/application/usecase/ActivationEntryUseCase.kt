package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

interface ActivationEntryUseCase {
    /**
     * 대기 중인 대기열 항목 중 작업 가능한 수만큼 활성화시킵니다.
     */
    fun activateEntries(): List<WaitingEntryDto>
}
