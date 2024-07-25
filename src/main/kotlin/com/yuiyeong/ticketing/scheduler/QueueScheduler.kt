package com.yuiyeong.ticketing.scheduler

import com.yuiyeong.ticketing.application.usecase.queue.ActivateWaitingEntriesUseCase
import com.yuiyeong.ticketing.application.usecase.queue.ExpireWaitingEntryUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["config.scheduler.enabled"], havingValue = "true", matchIfMissing = false)
class QueueScheduler(
    private val activateWaitingEntriesUseCase: ActivateWaitingEntriesUseCase,
    private val expireWaitingEntryUseCase: ExpireWaitingEntryUseCase,
) {
    @Scheduled(fixedRateString = "#{@schedulerProperties.queueFixedRate}")
    fun processQueue() {
        try {
            // token 만료
            val expiredEntries = expireWaitingEntryUseCase.execute()
            logger.info("Expired ${expiredEntries.size} entries.")

            // 대기 중인 항목 활성화
            val activatedEntries = activateWaitingEntriesUseCase.execute()
            logger.info("Activated ${activatedEntries.size} entries.")
        } catch (e: Exception) {
            logger.error("Error from processingQueue: ${e.message}", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(QueueScheduler::class.java)
    }
}
