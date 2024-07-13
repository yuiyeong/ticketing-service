package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

interface ExitingQueueUseCase {
    /**
     * 주어진 토큰에 해당하는 사용자를 대기열에서 퇴장시킵니다.
     */
    fun exit(token: String): WaitingEntryDto
}
