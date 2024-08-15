package com.yuiyeong.ticketing.domain.service.payment

import com.yuiyeong.ticketing.domain.model.payment.PaymentOutbox
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class PaymentOutboxService(
    private val paymentOutboxRepository: PaymentOutboxRepository,
) {
    /**
     * paymentId 를 가지고 CREATED 인 PaymentOutbox 를 찾아서 PUBLISHED 로 상태를 바꾸는 함수
     * PaymentOutbox 가 없다면, 아무것도 하지 않고 끝낸다.
     */
    @Transactional
    fun markAsPublishedIfExistByPaymentId(paymentId: Long) {
        val paymentOutbox = paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentId, PaymentOutboxStatus.CREATED) ?: return
        val publishedOne = paymentOutbox.markAsPublished()
        paymentOutboxRepository.save(publishedOne)
    }

    /**
     * moment 이후에 발행된지 않은(CREATED 인) PaymentOutbox 를 찾아서 반환하는 함수
     */
    fun findUnpublishedOutboxesBefore(moment: ZonedDateTime): List<PaymentOutbox> =
        paymentOutboxRepository.findAllByStatusAndPublishedTimeMilliBefore(
            PaymentOutboxStatus.CREATED,
            moment.toInstant().toEpochMilli(),
        )
}
