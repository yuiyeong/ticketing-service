package com.yuiyeong.ticketing.scheduler

import com.yuiyeong.ticketing.application.usecase.queue.ActivateWaitingEntriesUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["config.scheduler.enabled"], havingValue = "true", matchIfMissing = false)
class QueueScheduler(
    private val activateWaitingEntriesUseCase: ActivateWaitingEntriesUseCase,
) {
    // TODO N 계산하기
    @Scheduled(fixedRateString = "#{@schedulerProperties.queueFixedRate}")
    fun processQueue() {
        try {
            // 대기 중인 항목 활성화
            val activatedEntries = activateWaitingEntriesUseCase.execute()
            logger.info("Activated $activatedEntries entries.")
        } catch (e: Exception) {
            logger.error("Error from processingQueue: ${e.message}", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(QueueScheduler::class.java)
    }
}
