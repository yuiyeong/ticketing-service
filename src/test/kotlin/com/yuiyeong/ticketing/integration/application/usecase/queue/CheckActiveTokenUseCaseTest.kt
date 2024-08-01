package com.yuiyeong.ticketing.integration.application.usecase.queue

import com.yuiyeong.ticketing.application.usecase.queue.CheckActiveTokenUseCase
import com.yuiyeong.ticketing.domain.exception.TokenNotInActiveQueueException
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
class CheckActiveTokenUseCaseTest {
    @Autowired
    private lateinit var checkActiveTokenUseCase: CheckActiveTokenUseCase

    @Autowired
    private lateinit var redissonClient: RedissonClient

    @Autowired
    private lateinit var redisQueueRepository: RedisQueueRepository

    @BeforeEach
    fun beforeEach() {
        redissonClient.keys.flushall()
    }

    @Test
    fun `should pass when token is active`() {
        // given
        val token = "test-token"
        redisQueueRepository.addToWaitingQueue(token)
        redisQueueRepository.moveToActiveQueue(1)

        // when
        checkActiveTokenUseCase.execute(token)

        // then: nothing to do.
    }

    @Test
    fun `should throw TokenNotInActiveQueueException when trying to execute with a token not in any queue`() {
        // given
        val tokenNotInQueue = "tokenNotInQueue"

        // when & then
        Assertions
            .assertThatThrownBy { checkActiveTokenUseCase.execute(tokenNotInQueue) }
            .isInstanceOf(TokenNotInActiveQueueException::class.java)
    }

    @Test
    fun `should throw TokenNotInActiveQueueException when trying to execute with a token in waiting queue`() {
        // given
        val tokenInWaitingQueue = "tokenInWaitingQueue"
        redisQueueRepository.addToWaitingQueue(tokenInWaitingQueue)

        // when & then
        Assertions
            .assertThatThrownBy { checkActiveTokenUseCase.execute(tokenInWaitingQueue) }
            .isInstanceOf(TokenNotInActiveQueueException::class.java)
    }

    companion object {
        @Container
        private val redisContainer =
            GenericContainer(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379)
                .withCommand("redis-server", "--notify-keyspace-events", "KEA") // redis 의 이벤트를 받기 위한 command
    }
}
