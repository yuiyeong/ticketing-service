package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

interface ExpirationEntryUseCase {
    /**
     * 만료 시간이 지난 대기열 항목들을 만료시킵니다.
     */
    fun expireOverdueEntries(): List<WaitingEntryDto>
}
