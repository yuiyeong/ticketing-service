package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.queue.WaitingInfoResult

interface GetWaitingInfoUseCase {
    fun execute(token: String): WaitingInfoResult
}
