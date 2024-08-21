package com.yuiyeong.ticketing.integration.infrastructure.kafka

import com.yuiyeong.ticketing.domain.message.payment.PaymentMessage
import com.yuiyeong.ticketing.domain.message.payment.PaymentMessageProducer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.Duration
import kotlin.random.Random
import kotlin.test.Test

@SpringBootTest
@Testcontainers
class KafkaMessageProducerTest {
    @Value("\${config.kafka.topic.payment}")
    private lateinit var paymentTopic: String

    @Autowired
    private lateinit var kafkaProperties: KafkaProperties

    @Autowired
    private lateinit var paymentMessageProducer: PaymentMessageProducer

    @Test
    fun `should receive multiple payment messages after PaymentMessagePublisher publishes the messages`() {
        // given: 실패 결제 메시지와 성공 결제 메시지가 각각 1개씩 있고, PaymentMessage 를 구독하는 consumer 가 있는 상황
        val paymentMessage0 = createRandomPaymentMessage(true) as PaymentMessage.Failure // Failure
        val paymentMessage1 = createRandomPaymentMessage(false) // Success

        val consumer = createConsumer<PaymentMessage>()
        consumer.subscribe(listOf(paymentTopic))

        // when: 실패 결제 메시지, 성공 결제 메시지 순으로 보냄
        paymentMessageProducer.send(paymentMessage0)
        paymentMessageProducer.send(paymentMessage1)

        // then: 총 2개의 records 를 읽게 되고, records 에는 실패 결제 메시지, 성공 결제 메시지가 순서대로 들어 있어야 한다.
        val records = consumer.poll(Duration.ofSeconds(2))
        Assertions.assertThat(records.count()).isEqualTo(2)

        val failureMessage = records.first().value() as PaymentMessage.Failure
        Assertions.assertThat(failureMessage.userId).isEqualTo(paymentMessage0.userId)
        Assertions.assertThat(failureMessage.reservationId).isEqualTo(paymentMessage0.reservationId)
        Assertions.assertThat(failureMessage.amount).isEqualByComparingTo(paymentMessage0.amount)
        Assertions.assertThat(failureMessage.publishedTimeMilli).isEqualTo(paymentMessage0.publishedTimeMilli)
        Assertions.assertThat(failureMessage.failureReason).isEqualTo(paymentMessage0.failureReason)

        val successMessage = records.last().value() as PaymentMessage.Success
        Assertions.assertThat(successMessage.userId).isEqualTo(paymentMessage1.userId)
        Assertions.assertThat(successMessage.reservationId).isEqualTo(paymentMessage1.reservationId)
        Assertions.assertThat(successMessage.amount).isEqualByComparingTo(paymentMessage1.amount)
        Assertions.assertThat(successMessage.publishedTimeMilli).isEqualTo(paymentMessage1.publishedTimeMilli)

        consumer.unsubscribe()
        consumer.close()
    }

    private fun <T> createConsumer(): KafkaConsumer<String, T> =
        KafkaConsumer<String, T>(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to "test-group-0",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                JsonDeserializer.TRUSTED_PACKAGES to "com.yuiyeong.ticketing.*",
            ),
        )

    private fun createRandomPaymentMessage(asFailure: Boolean): PaymentMessage {
        val random = Random(System.currentTimeMillis())
        return createPaymentMessage(
            userId = random.nextLong(100),
            reservationId = random.nextLong(100, 1_000),
            paymentId = random.nextLong(100, 1_000),
            amount = BigDecimal(random.nextInt(1_000, 10_000)),
            publishedTimeMillis = System.currentTimeMillis(),
            failureReason = if (asFailure) "test-fail-reason-${random.nextInt(100)}" else null,
        )
    }

    private fun createPaymentMessage(
        userId: Long,
        reservationId: Long,
        paymentId: Long,
        amount: BigDecimal,
        publishedTimeMillis: Long,
        failureReason: String?,
    ): PaymentMessage =
        if (failureReason != null) {
            PaymentMessage.Failure(userId, reservationId, paymentId, amount, publishedTimeMillis, failureReason)
        } else {
            PaymentMessage.Success(userId, reservationId, paymentId, amount, publishedTimeMillis)
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
