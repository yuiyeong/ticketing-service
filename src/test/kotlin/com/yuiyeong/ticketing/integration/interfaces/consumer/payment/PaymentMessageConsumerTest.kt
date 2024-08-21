package com.yuiyeong.ticketing.integration.interfaces.consumer.payment

import com.yuiyeong.ticketing.application.usecase.notification.SendPaymentNotificationUseCase
import com.yuiyeong.ticketing.application.usecase.payment.MarkPaymentOutboxAsPublishedUseCase
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessageProducer
import com.yuiyeong.ticketing.domain.model.payment.PaymentOutboxStatus
import com.yuiyeong.ticketing.domain.notification.PaymentNotificationService
import com.yuiyeong.ticketing.domain.repository.payment.PaymentOutboxRepository
import com.yuiyeong.ticketing.helper.TestDataFactory.createRandomPaymentOutbox
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.Executors
import kotlin.test.Test

@SpringBootTest
@Testcontainers
class PaymentMessageConsumerTest {
    @Autowired
    private lateinit var paymentMessageProducer: PaymentMessageProducer

    @Autowired
    private lateinit var paymentOutboxRepository: PaymentOutboxRepository

    // 실제로 메시지 발송되는 것 방지
    @MockBean
    private lateinit var paymentNotificationService: PaymentNotificationService

    @SpyBean
    private lateinit var markPaymentOutboxAsPublishedUseCase: MarkPaymentOutboxAsPublishedUseCase

    @SpyBean
    private lateinit var sendPaymentNotificationUseCase: SendPaymentNotificationUseCase

    @BeforeEach
    fun beforeEach() {
        reset(markPaymentOutboxAsPublishedUseCase)
        reset(sendPaymentNotificationUseCase)
    }

    @Test
    fun `should mark a paymentOutbox as PUBLISHED`() {
        // given: CREATED 인 paymentOutbox 1개
        val paymentOutbox = paymentOutboxRepository.save(createRandomPaymentOutbox("test-failure-reason"))
        val paymentMessage = PaymentMessage.createFrom(paymentOutbox.extractPaymentEvent())

        // when: PaymentMessage 보냄
        paymentMessageProducer.send(paymentMessage)
        Thread.sleep(1000) // Wait for message to be consumed

        // then: 해당 메시지를 받아서 소비되었을 것이기 때문에, PaymentOutbox 가 PUBLISHED 가 되어야한다.
        val publishedOutbox = paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.PUBLISHED)
        Assertions.assertThat(publishedOutbox).isNotNull()
        Assertions.assertThat(publishedOutbox?.status).isEqualTo(PaymentOutboxStatus.PUBLISHED)
    }

    @Test
    fun `should do nothing when receiving paymentMessage that is already handled`() {
        // given: 이미 발행 처리된 PaymentMessage 에 대해서
        val paymentOutbox = paymentOutboxRepository.save(createRandomPaymentOutbox().markAsPublished())
        val paymentMessage = PaymentMessage.createFrom(paymentOutbox.extractPaymentEvent())

        // when: 중복 발행
        paymentMessageProducer.send(paymentMessage)
        Thread.sleep(1000) // Wait for message to be consumed

        // then: 다시 소비를 해도, 이미 처리되었기때문에 PaymentOutbox 는 변화가 없음
        val publishedOutbox = paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.PUBLISHED)
        Assertions.assertThat(publishedOutbox).isNotNull()
        Assertions.assertThat(publishedOutbox?.status).isEqualTo(PaymentOutboxStatus.PUBLISHED)
    }

    @Test
    fun `should receive paymentMessage all of kafkaListeners`() {
        // given
        val paymentOutbox = paymentOutboxRepository.save(createRandomPaymentOutbox())
        val paymentMessage = PaymentMessage.createFrom(paymentOutbox.extractPaymentEvent())

        doNothing().`when`(markPaymentOutboxAsPublishedUseCase).execute(paymentMessage.paymentId)
        doNothing().`when`(sendPaymentNotificationUseCase).execute(paymentMessage)

        // when
        paymentMessageProducer.send(paymentMessage)
        Thread.sleep(1000) // Wait for message to be consumed

        // then: 각 consumer 에서 사용한 UseCase 가 1번씩은 호출되어야 함
        verify(markPaymentOutboxAsPublishedUseCase, times(1)).execute(paymentOutbox.paymentId)
        verify(sendPaymentNotificationUseCase, times(1)).execute(paymentMessage)
    }

    @Test
    fun `should handle concurrent paymentMessages for the same payment`() {
        // given
        val paymentOutbox = paymentOutboxRepository.save(createRandomPaymentOutbox())
        val paymentMessage = PaymentMessage.createFrom(paymentOutbox.extractPaymentEvent())

        // when: 같은 PaymentMessage 를 동시에 5번 보냄
        val executorService = Executors.newFixedThreadPool(5)
        val futures =
            (1..5).map {
                executorService.submit { paymentMessageProducer.send(paymentMessage) }
            }
        futures.forEach { it.get() } // Wait for all messages to be sent
        Thread.sleep(1000) // Wait for messages to be consumed

        // then: 몇번이고 처리되어도 PaymentOutbox 는 PUBLISHED 이어야 한다.
        val publishedOutbox = paymentOutboxRepository.findOneByPaymentIdAndStatus(paymentOutbox.paymentId, PaymentOutboxStatus.PUBLISHED)
        Assertions.assertThat(publishedOutbox).isNotNull()
        Assertions.assertThat(publishedOutbox?.status).isEqualTo(PaymentOutboxStatus.PUBLISHED)
    }

    companion object {
        @Container
        private val kafkaContainer = KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"))

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { kafkaContainer.bootstrapServers }
        }
    }
}
