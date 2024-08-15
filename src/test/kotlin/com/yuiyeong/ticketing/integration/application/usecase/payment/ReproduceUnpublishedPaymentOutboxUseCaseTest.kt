package com.yuiyeong.ticketing.integration.application.usecase.payment

import com.yuiyeong.ticketing.application.usecase.payment.ReproduceUnpublishedPaymentOutboxUseCase
import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessageProducer
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import com.yuiyeong.ticketing.helper.TestDataFactory.createRandomPaymentOutbox
import org.assertj.core.api.Assertions
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import kotlin.test.Test

@Transactional
@SpringBootTest
class ReproduceUnpublishedPaymentOutboxUseCaseTest {
    @Value("\${config.outbox.threshold-as-min}")
    private val outboxThresholdAsMin: Long = 0

    @Autowired
    private lateinit var reproduceUnpublishedPaymentOutboxUseCase: ReproduceUnpublishedPaymentOutboxUseCase

    @Autowired
    private lateinit var paymentOutboxRepository: PaymentOutboxRepository

    @MockBean
    private lateinit var paymentMessageProducer: PaymentMessageProducer

    @Test
    fun `should return count of reproducing payment outboxes`() {
        // given
        val publishedTimeMilli =
            ZonedDateTime
                .now()
                .minusHours(1)
                .asUtc
                .toInstant()
                .toEpochMilli()
        paymentOutboxRepository.save(createRandomPaymentOutbox().copy(publishedTimeMilli = publishedTimeMilli))
        paymentOutboxRepository.save(createRandomPaymentOutbox().copy(publishedTimeMilli = publishedTimeMilli + 1))

        doNothing().`when`(paymentMessageProducer).send(any())

        // when
        val count = reproduceUnpublishedPaymentOutboxUseCase.execute()

        // then: 재발행을 2번 해야한다.
        Assertions.assertThat(count).isEqualTo(2)
        verify(paymentMessageProducer, times(2)).send(any())
    }

    @Test
    fun `should return zero count when there is no unpublished payment outboxes`() {
        // given: PUBLISHED 인 paymentOutbox 1개 와 발행 시도한 지 outboxThresholdAsMin 을 지나지 않은 paymentOutbox 1개
        val publishedTimeMilli =
            ZonedDateTime
                .now()
                .minusMinutes(outboxThresholdAsMin - 1)
                .asUtc
                .toInstant()
                .toEpochMilli()
        paymentOutboxRepository.save(createRandomPaymentOutbox().markAsPublished())
        paymentOutboxRepository.save(createRandomPaymentOutbox().copy(publishedTimeMilli = publishedTimeMilli + 1))

        doNothing().`when`(paymentMessageProducer).send(any())

        // when
        val count = reproduceUnpublishedPaymentOutboxUseCase.execute()

        // then: 재발행을 할게 아무것도 없어야 한다.
        Assertions.assertThat(count).isEqualTo(0)
        verify(paymentMessageProducer, never()).send(any())
    }
}
