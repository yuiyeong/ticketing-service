package com.yuiyeong.ticketing.integration.application.usecase.queue

import com.yuiyeong.ticketing.application.usecase.queue.EnterQueueUseCase
import com.yuiyeong.ticketing.config.property.QueueProperties
import com.yuiyeong.ticketing.infrastructure.redis.repository.RedisQueueRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test

@Testcontainers
@SpringBootTest
class EnterQueueUseCaseTest {
    @Autowired
    private lateinit var enterQueueUseCase: EnterQueueUseCase

    @Autowired
    private lateinit var queueProperties: QueueProperties

    @Autowired
    private lateinit var redissonClient: RedissonClient

    @Autowired
    private lateinit var redisQueueRepository: RedisQueueRepository

    @BeforeEach
    fun beforeEach() {
        redissonClient.keys.flushall()
    }

    @Test
    fun `should return WaitingInfoResult after entering a waiting queue`() {
        // given: 대기열에 10개의 token 이 있고, userId 가 있는 상황
        val tokenCount = 10
        (1..tokenCount).forEach { redisQueueRepository.addToWaitingQueue("test-token-$it") }
        val userId = 78L

        // when: 대기열에 진입
        val waitingInfo = enterQueueUseCase.execute(userId)

        // then: 11번째로 대기열에 진입했다는 대기 정보가 와야한다.
        val expectedPosition = 11
        Assertions.assertThat(waitingInfo.position).isEqualTo(expectedPosition)
        Assertions.assertThat(waitingInfo.estimatedWaitingTime).isEqualTo(expectedPosition * queueProperties.estimatedWorkingTimeInMinutes)
    }

    @Test
    fun `should should return WaitingInfoResult that has last queue position when trying to entering a waiting queue several times`() {
        // given
        val userId = 8L
        val firstEnteringResult = enterQueueUseCase.execute(userId)
        enterQueueUseCase.execute(332L)
        enterQueueUseCase.execute(32L)

        // when
        val secondEnteringResult = enterQueueUseCase.execute(userId)

        // then
        Assertions.assertThat(firstEnteringResult.token).isNotEqualTo(secondEnteringResult.token)
        Assertions.assertThat(firstEnteringResult.position).isNotEqualTo(secondEnteringResult.position)
        Assertions.assertThat(secondEnteringResult.position).isNotEqualTo(3)
    }

    companion object {
        @Container
        private val redisContainer =
            GenericContainer(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379)
                .withCommand("redis-server", "--notify-keyspace-events", "KEA") // redis 의 이벤트를 받기 위한 command
    }
}
