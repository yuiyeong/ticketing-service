package com.yuiyeong.ticketing.integration.infrastructure.redis.repository

import com.yuiyeong.ticketing.config.QueueProperties
import com.yuiyeong.ticketing.infrastructure.redis.repository.RedisQueueRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test

@Testcontainers
class RedisQueueRepositoryTest {
    private lateinit var redissonClient: RedissonClient

    @BeforeEach
    fun beforeEach() {
        redissonClient =
            Redisson.create(
                Config().apply {
                    useSingleServer().address = "redis://${redisContainer.host}:${redisContainer.firstMappedPort}"
                },
            )
        redissonClient.keys.flushall()
    }

    @Test
    fun `should fail when token already exists`() {
        // given
        val token = "testToken"

        // when
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)
        val firstResult = redisQueueRepository.addToWaitingQueue(token)
        val secondResult = redisQueueRepository.addToWaitingQueue(token)

        // then
        Assertions.assertThat(firstResult).isTrue()
        Assertions.assertThat(secondResult).isFalse()
    }

    @Test
    fun `should return null for non-existent token`() {
        // given
        val nonExistentToken = "nonExistentToken"

        // when
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)
        val position = redisQueueRepository.getWaitingQueuePosition(nonExistentToken)

        // then
        Assertions.assertThat(position).isNull()
    }

    @Test
    fun `should return false for non-existent token`() {
        // given
        val nonExistentToken = "nonExistentToken"

        // when
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)
        val result = redisQueueRepository.removeFromQueue(nonExistentToken)

        // then
        Assertions.assertThat(result).isFalse()
    }

    @Test
    fun `should handle partial success`() {
        // given
        val tokens = (1..10).map { "token$it" }
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)
        tokens.forEach { redisQueueRepository.addToWaitingQueue(it) }

        // Simulate a situation where the active queue can only accept 5 tokens
        repeat(5) { redisQueueRepository.moveToActiveQueue(1) }

        // when
        val movedCount = redisQueueRepository.moveToActiveQueue(10)

        // then
        Assertions.assertThat(movedCount).isEqualTo(5)
        Assertions.assertThat(redisQueueRepository.getActiveQueueSize()).isEqualTo(10)
    }

    @Test
    fun `should be removed from all queues when it expires`() {
        // given
        val properties = QueueProperties().apply { tokenTtlInSeconds = 2L }
        val redisQueueRepository = RedisQueueRepository(properties, redissonClient)

        val token = "expiringToken"

        // when
        redisQueueRepository.addToWaitingQueue(token)
        redisQueueRepository.moveToActiveQueue(1)

        // Wait for the token to expire
        Thread.sleep(5000)

        // then
        Assertions.assertThat(redisQueueRepository.getWaitingQueuePosition(token)).isNull()
        Assertions.assertThat(redisQueueRepository.isInActiveQueue(token)).isFalse()
    }

    @Test
    fun `should add token to waiting queue`() {
        // given
        val token = "testToken"

        // when
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)
        val result = redisQueueRepository.addToWaitingQueue(token)

        // then
        Assertions.assertThat(result).isTrue()
        Assertions.assertThat(redisQueueRepository.getWaitingQueuePosition(token)).isEqualTo(1)
    }

    @Test
    fun `should move correct small number of tokens`() {
        // given
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)

        val tokenCount = 10
        repeat(tokenCount) { i -> redisQueueRepository.addToWaitingQueue("token$i") }

        // when
        val movedCount = redisQueueRepository.moveToActiveQueue(5)

        // then
        Assertions.assertThat(movedCount).isEqualTo(5)
        Assertions.assertThat(redisQueueRepository.getActiveQueueSize()).isEqualTo(5)
        Assertions.assertThat(redisQueueRepository.getWaitingQueuePosition("token4")).isNull()
        Assertions.assertThat(redisQueueRepository.getWaitingQueuePosition("token5")).isEqualTo(1)
    }

    @Test
    fun `should move correct number of tokens when number is over batchSize`() {
        // given
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)

        val tokenCount = 1500
        repeat(tokenCount) { i -> redisQueueRepository.addToWaitingQueue("token$i") }

        // when
        val movedCount = redisQueueRepository.moveToActiveQueue(1000)

        // then
        Assertions.assertThat(movedCount).isEqualTo(1000)
        Assertions.assertThat(redisQueueRepository.getActiveQueueSize()).isEqualTo(1000)
        Assertions.assertThat(redisQueueRepository.getWaitingQueuePosition("token999")).isNull()
        Assertions.assertThat(redisQueueRepository.getWaitingQueuePosition("token1000")).isEqualTo(1)
    }

    @Test
    fun `should remove token from active queue`() {
        // given
        val redisQueueRepository = RedisQueueRepository(QueueProperties(), redissonClient)

        val token = "testToken"
        redisQueueRepository.addToWaitingQueue(token)
        redisQueueRepository.moveToActiveQueue(1)

        // when
        val result = redisQueueRepository.removeFromQueue(token)

        // then
        Assertions.assertThat(result).isTrue()
        Assertions.assertThat(redisQueueRepository.isInActiveQueue(token)).isFalse()
    }

    companion object {
        @Container
        private val redisContainer =
            GenericContainer(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379)
                .withCommand("redis-server", "--notify-keyspace-events", "KEA")
    }
}
