package com.yuiyeong.ticketing.integration.infrastructure.kafka

import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.test.Test
import kotlin.time.Duration.Companion.nanoseconds

@SpringBootTest
@Testcontainers
class KafkaConnectionTest {
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @Autowired
    private lateinit var consumerFactory: ConsumerFactory<String, Any>

    @Test
    fun `should receive a message that is sent by kafkaTemplate`() {
        // given
        val testTopic = "topic-for-test"
        val testKey = "test-key"
        val totalMessageCount = 10
        val testMessages = (0..<totalMessageCount).map { "test-message-$it-${System.currentTimeMillis().nanoseconds}" }

        val testGroupId = "test-group-0"
        val consumer = consumerFactory.createConsumer(testGroupId, null)
        consumer.subscribe(listOf(testTopic))

        // when: 테스트 메시지가 순서대로 보내짐
        testMessages.forEach { kafkaTemplate.send(testTopic, testKey, it) }

        // then: 총 totalMessageCount 개수 만큼의 메시지를 순서대로 받음
        val records = consumer.poll(Duration.ofSeconds(3))
        Assertions.assertThat(records.count()).isEqualTo(totalMessageCount)

        records.forEachIndexed { index, record ->
            Assertions.assertThat(record.key()).isEqualTo(testKey)
            Assertions.assertThat(record.value()).isEqualTo(testMessages[index])
        }
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
