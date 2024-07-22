package com.yuiyeong.ticketing.scheduler

import com.yuiyeong.ticketing.application.usecase.occupation.ExpireOccupationsUseCase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OccupationScheduler(
    private val expireOccupationsUseCase: ExpireOccupationsUseCase,
) {
    @Scheduled(fixedRateString = "#{@schedulerProperties.occupationFixedRate}")
    fun expireOverdueOccupations() {
        try {
            // 점유 만료
            val expiredOccupations = expireOccupationsUseCase.execute()
            logger.info("Expired ${expiredOccupations.size} occupations.")
        } catch (e: Exception) {
            logger.error("Error from expireOverdueOccupations: ${e.message}", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OccupationScheduler::class.java)
    }
}
