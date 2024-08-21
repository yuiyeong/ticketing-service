package com.yuiyeong.ticketing.integration.infrastructure.repository

import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import com.yuiyeong.ticketing.helper.TestDataFactory.createRandomPaymentOutbox
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
class PaymentOutboxRepositoryTest {
    @Autowired
    private lateinit var paymentOutboxRepository: PaymentOutboxRepository

    @Test
    fun `should return a paymentOutbox after saving it`() {
        // given
        val paymentOutbox = createRandomPaymentOutbox("test failure reason")

        // when
        val savedOne = paymentOutboxRepository.save(paymentOutbox)

        // then
        Assertions.assertThat(savedOne.id).isNotEqualTo(paymentOutbox.id)
        Assertions.assertThat(savedOne.payload).isEqualTo(paymentOutbox.payload)
        Assertions.assertThat(savedOne.status).isEqualTo(paymentOutbox.status)
        Assertions.assertThat(savedOne.publishedTimeMilli).isEqualTo(paymentOutbox.publishedTimeMilli)
    }

    @Test
    fun `should return only created paymentOutboxes when finding paymentOutboxes with CREATED status`() {
        // given: CREATED 가 2개, PUBLISHED 가 1개
        val createdOutbox1 = paymentOutboxRepository.save(createRandomPaymentOutbox())
        paymentOutboxRepository.save(createRandomPaymentOutbox().markAsPublished())
        val createdOutbox2 = paymentOutboxRepository.save(createRandomPaymentOutbox())
        val moment = System.currentTimeMillis()

        // when
        val paymentOutboxes = paymentOutboxRepository.findAllByStatusAndPublishedTimeMilliBefore(PaymentOutboxStatus.CREATED, moment)

        // then: CREATED 2개만 찾는다.
        Assertions.assertThat(paymentOutboxes.size).isEqualTo(2)
        Assertions.assertThat(paymentOutboxes[0].id).isEqualTo(createdOutbox1.id)
        Assertions.assertThat(paymentOutboxes[1].id).isEqualTo(createdOutbox2.id)
    }

    @Test
    fun `should return only created paymentOutbox that has a paymentId when finding payment outbox by CREATED status`() {
        // given: CREATED 만 있는 상황
        val paymentOutbox = paymentOutboxRepository.save(createRandomPaymentOutbox())

        // when: 같은 paymentId 와 CREATED 로 찾음
        val foundOne = paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.CREATED)

        // then: 해당 paymentOutbox 를 찾아야 한다.
        Assertions.assertThat(foundOne).isNotNull()
        Assertions.assertThat(foundOne!!.id).isEqualTo(paymentOutbox.id)
        Assertions.assertThat(foundOne.paymentId).isEqualTo(paymentOutbox.paymentId)
        Assertions.assertThat(foundOne.payload).isEqualTo(paymentOutbox.payload)
        Assertions.assertThat(foundOne.publishedTimeMilli).isEqualTo(paymentOutbox.publishedTimeMilli)
        Assertions.assertThat(foundOne.status).isEqualTo(paymentOutbox.status)
    }

    @Test
    fun `should return null that has a paymentId when finding payment outbox by CREATED status`() {
        // given: PUBLISHED 만 있는 상황
        val paymentOutbox = paymentOutboxRepository.save(createRandomPaymentOutbox().markAsPublished())

        // when: 같은 paymentId 와 CREATED 로 찾음
        val foundOne = paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.CREATED)

        // then: null 이어야 한다.
        Assertions.assertThat(foundOne).isNull()
    }
}
