package com.yuiyeong.ticketing.integration.domain.service.payment

import com.yuiyeong.ticketing.common.asUtc
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import com.yuiyeong.ticketing.domain.service.payment.PaymentOutboxService
import com.yuiyeong.ticketing.helper.TestDataFactory.createRandomPaymentOutbox
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import kotlin.test.Test

@SpringBootTest
@Transactional
class PaymentOutboxServiceTest {
    @Autowired
    private lateinit var paymentOutboxService: PaymentOutboxService

    @Autowired
    private lateinit var paymentOutboxRepository: PaymentOutboxRepository

    @Test
    fun `should mark CREATED paymentOutbox as PUBLISHED`() {
        // given: CREATED 인 paymentOutbox 1 개
        val paymentOutbox = paymentOutboxRepository.save(createRandomPaymentOutbox())
        val paymentId = paymentOutbox.paymentId

        // when: PUBLISHED 로 변경
        paymentOutboxService.markAsPublishedIfExistByPaymentId(paymentId)

        // then: paymentId 를 가지고 CREATED 인 PaymentOutbox 는 없고, PUBLISHED 인 것만 있다.
        Assertions
            .assertThat(
                paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentId, PaymentOutboxStatus.CREATED),
            ).isNull()
        Assertions
            .assertThat(
                paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentId, PaymentOutboxStatus.PUBLISHED),
            ).isNotNull()
    }

    @Test
    fun `should return unpublished paymentOutboxes after a moment`() {
        // given: CREATED 이고 서로 다른 paymentId 를 가지는 PaymentOutbox 2 개와 PUBLISHED 인 PaymentOutbox 1개
        val paymentOutbox1 = paymentOutboxRepository.save(createRandomPaymentOutbox("test failure reason"))
        paymentOutboxRepository.save(createRandomPaymentOutbox().markAsPublished())
        val paymentOutbox2 = paymentOutboxRepository.save(createRandomPaymentOutbox())
        val moment = ZonedDateTime.now().asUtc

        // when: 현재 시간 이전에서, 발행되지 않은 paymentOutbox 찾기
        val outboxes = paymentOutboxService.findUnpublishedOutboxesBefore(moment)

        // then: 2개의 CREATED paymentOutbox 가 있어야 한다.
        Assertions.assertThat(outboxes.size).isEqualTo(2)
        Assertions.assertThat(outboxes[0].id).isEqualTo(paymentOutbox1.id)
        Assertions.assertThat(outboxes[0].status).isEqualTo(PaymentOutboxStatus.CREATED)
        Assertions.assertThat(outboxes[1].id).isEqualTo(paymentOutbox2.id)
        Assertions.assertThat(outboxes[1].status).isEqualTo(PaymentOutboxStatus.CREATED)
    }

    @Test
    fun `should return empty when there is no unpublished paymentOutbox`() {
        // given: PUBLISHED 이고 서로 다른 paymentId 를 가지는 PaymentOutbox 3 개
        paymentOutboxRepository.save(createRandomPaymentOutbox().markAsPublished())
        paymentOutboxRepository.save(createRandomPaymentOutbox("test failure reason").markAsPublished())
        paymentOutboxRepository.save(createRandomPaymentOutbox("test failure reason").markAsPublished())
        val moment = ZonedDateTime.now().asUtc

        // when
        val outboxes = paymentOutboxService.findUnpublishedOutboxesBefore(moment)

        // then: 아무것도 없어야 한다.
        Assertions.assertThat(outboxes).isEmpty()
    }
}
