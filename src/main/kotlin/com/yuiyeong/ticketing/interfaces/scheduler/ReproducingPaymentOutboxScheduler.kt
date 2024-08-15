package com.yuiyeong.ticketing.interfaces.scheduler

import com.yuiyeong.ticketing.application.usecase.payment.ReproduceUnpublishedPaymentOutboxUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "config.scheduler", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class ReproducingPaymentOutboxScheduler(
    private val reproduceUnpublishedPaymentOutboxUseCase: ReproduceUnpublishedPaymentOutboxUseCase,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    @Scheduled(fixedRateString = "\${config.scheduler.reproducing-fixed-rate}")
    fun reproduceUnpublishedPaymentOutboxes() {
        try {
            // 발행되지 않은 PaymentOutbox 재발행
            val reproducedCount = reproduceUnpublishedPaymentOutboxUseCase.execute()
            logger.info("Reproduce [$reproducedCount] payment-outboxes")
        } catch (e: Exception) {
            logger.error("Error from reproducingUnpublishedPaymentOutboxes; ${e.message}", e)
        }
    }
}
