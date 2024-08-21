package com.yuiyeong.ticketing.unit.domain.service.payment

import com.yuiyeong.ticketing.domain.model.payment.PaymentOutbox
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import com.yuiyeong.ticketing.domain.service.payment.PaymentOutboxService
import com.yuiyeong.ticketing.helper.TestDataFactory.createRandomPaymentOutbox
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class PaymentOutboxServiceTest {
    @Mock
    private lateinit var paymentOutboxRepository: PaymentOutboxRepository

    private lateinit var paymentOutboxService: PaymentOutboxService

    @BeforeEach
    fun beforeEach() {
        paymentOutboxService = PaymentOutboxService(paymentOutboxRepository)
    }

    @Test
    fun `should mark a paymentOutbox as published`() {
        // given
        val paymentOutbox = createRandomPaymentOutbox()
        given(
            paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.CREATED),
        ).willReturn(paymentOutbox)
        given(paymentOutboxRepository.save(any())).willAnswer { invocation ->
            val savedOne = invocation.getArgument<PaymentOutbox>(0)
            savedOne.copy(id = 1L)
        }

        // when
        paymentOutboxService.markAsPublishedIfExistByPaymentId(paymentOutbox.paymentId)

        // then
        verify(paymentOutboxRepository).findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.CREATED)
        verify(paymentOutboxRepository).save(
            argThat { it -> it.paymentId == paymentOutbox.paymentId && it.status == PaymentOutboxStatus.PUBLISHED },
        )
    }

    @Test
    fun `should do nothing when there is no CREATED paymentOutbox`() {
        // given
        val paymentOutbox = createRandomPaymentOutbox()
        given(
            paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.CREATED),
        ).willReturn(null)

        // when
        paymentOutboxService.markAsPublishedIfExistByPaymentId(paymentOutbox.paymentId)

        // then
        verify(paymentOutboxRepository).findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.CREATED)
        verify(paymentOutboxRepository, never()).save(any())
    }
}
