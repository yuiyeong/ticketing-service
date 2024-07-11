package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.WaitingEntryDto

interface QueueEntryInfoUseCase {
    /**
     * 주어진 토큰에 해당하는 대기열 항목 정보를 조회합니다.
     */
    fun getEntry(token: String?): WaitingEntryDto
}
