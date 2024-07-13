package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

interface EnteringQueueUseCase {
    /**
     * 사용자를 대기열에 입장시킵니다.
     */
    fun enter(userId: Long): WaitingEntryDto
}
