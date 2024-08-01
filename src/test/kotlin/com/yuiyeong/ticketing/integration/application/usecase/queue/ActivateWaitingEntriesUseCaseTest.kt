package com.yuiyeong.ticketing.integration.application.usecase.queue

import com.yuiyeong.ticketing.application.usecase.queue.ActivateWaitingEntriesUseCase
import com.yuiyeong.ticketing.infrastructure.redis.repository.RedisQueueRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test
import kotlin.time.Duration.Companion.nanoseconds

@SpringBootTest
@Transactional
class ActivateWaitingEntriesUseCaseTest {
    @Autowired
    private lateinit var activateWaitingEntriesUseCase: ActivateWaitingEntriesUseCase

    @Autowired
    private lateinit var redissonClient: RedissonClient

    @Autowired
    private lateinit var redisQueueRepository: RedisQueueRepository

    @BeforeEach
    fun beforeEach() {
        redissonClient.keys.flushall()
    }

    @Test
    fun `should return a count for activating tokens in waiting queue`() {
        // given: 10개의 token 이 대기열에 있는 상황
        val tokenCount = 10
        (1..tokenCount).forEachIndexed { _, i ->
            redisQueueRepository.addToWaitingQueue("test-waiting-token-$i")
        }

        // when
        val activatedCount = activateWaitingEntriesUseCase.execute()

        // then: 최대 처리 가능한 순번의 token 까지 활성 Queue 로 옯겼을 것이므로,
        //       activatedCount 는 대기열에 있는 총 token 수와 같아야한다.
        Assertions.assertThat(activatedCount).isEqualTo(tokenCount)
    }

    @Test
    fun `should return null after activateWaitingEntries`() {
        // given: 1개의 token 이 대기열에 있는 상황
        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        redisQueueRepository.addToWaitingQueue(token)

        // when
        activateWaitingEntriesUseCase.execute()

        // then: 대기 정보는 없고, 활성 상태여야한다.
        Assertions.assertThat(redisQueueRepository.isInActiveQueue(token)).isTrue()
        Assertions.assertThat(redisQueueRepository.isInWaitingQueue(token)).isFalse()
    }

    companion object {
        @Container
        private val redisContainer =
            GenericContainer(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379)
                .withCommand("redis-server", "--notify-keyspace-events", "KEA") // redis 의 이벤트를 받기 위한 command
    }
}
