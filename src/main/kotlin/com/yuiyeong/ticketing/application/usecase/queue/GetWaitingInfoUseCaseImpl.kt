package com.yuiyeong.ticketing.application.usecase.queue

import com.yuiyeong.ticketing.application.dto.queue.WaitingInfoResult
import com.yuiyeong.ticketing.domain.model.queue.WaitingInfo
import com.yuiyeong.ticketing.domain.service.queue.QueueService
import org.springframework.stereotype.Component

@Component
class GetWaitingInfoUseCaseImpl(
    private val queueService: QueueService,
) : GetWaitingInfoUseCase {
    override fun execute(token: String): WaitingInfoResult {
        queueService.verifyTokenIsInAnyQueue(token)

        val waitingInfo =
            queueService.getWaitingInfo(token) ?: WaitingInfo(
                token,
                POSITION_NOT_IN_WAITING_QUEUE,
                WAITING_TIME_NOT_IN_WAITING_QUEUE,
            )
        return WaitingInfoResult.from(waitingInfo)
    }

    companion object {
        private const val POSITION_NOT_IN_WAITING_QUEUE = 0
        private const val WAITING_TIME_NOT_IN_WAITING_QUEUE = 0L
    }
}
