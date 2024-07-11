package com.yuiyeong.ticketing.application.scheduler

import com.yuiyeong.ticketing.application.service.QueueService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class QueueScheduler(
    @Autowired val queueService: QueueService,
) {
    @Scheduled(fixedRate = 5000) // 5초 뒤부터, 5초 마다 실행
    fun processQueue() {
        try {
            // token 만료
            val expiredEntries = queueService.expireOverdueEntries()
            logger.info("Expired ${expiredEntries.size} entries.")

            // 대기 중인 항목 활성화
            val activatedEntries = queueService.activateWaitingEntries()
            logger.info("Activated ${activatedEntries.size} entries.")
        } catch (e: Exception) {
            logger.error("Error from processingQueue: ${e.message}", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(QueueScheduler::class.java)
    }
}
