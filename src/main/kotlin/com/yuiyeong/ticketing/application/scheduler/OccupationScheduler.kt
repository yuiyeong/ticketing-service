package com.yuiyeong.ticketing.application.scheduler

import com.yuiyeong.ticketing.application.usecase.reservation.ExpireOccupationUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OccupationScheduler {
    @Autowired
    private lateinit var expireOccupationUseCase: ExpireOccupationUseCase

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    fun expireOverdueOccupations() {
        try {
            // token 만료
            val expiredOccupations = expireOccupationUseCase.execute()
            logger.info("Expired ${expiredOccupations.size} occupations.")
        } catch (e: Exception) {
            logger.error("Error from expireOverdueOccupations: ${e.message}", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OccupationScheduler::class.java)
    }
}