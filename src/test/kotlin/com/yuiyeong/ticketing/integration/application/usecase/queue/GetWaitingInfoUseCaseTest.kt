package com.yuiyeong.ticketing.integration.application.usecase.queue

import com.yuiyeong.ticketing.application.usecase.queue.GetWaitingInfoUseCase
import com.yuiyeong.ticketing.config.property.QueueProperties
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.infrastructure.redis.repository.RedisQueueRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.redisson.api.RedissonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test
import kotlin.time.Duration.Companion.nanoseconds

@SpringBootTest
@Testcontainers
class GetWaitingInfoUseCaseTest {
    @Autowired
    private lateinit var getWaitingInfoUseCase: GetWaitingInfoUseCase

    @Autowired
    private lateinit var redisQueueRepository: RedisQueueRepository

    @Autowired
    private lateinit var queueProperties: QueueProperties

    @Autowired
    private lateinit var redissonClient: RedissonClient

    @BeforeEach
    fun beforeEach() {
        redissonClient.keys.flushall()
    }

    @Test
    fun `should return WaitingInfo when token is in the queue`() {
        // given: 대기열에서 token 앞에 1개의 다른 token 이 있는 상황
        redisQueueRepository.addToWaitingQueue("first-waiting-token")

        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        redisQueueRepository.addToWaitingQueue(token)

        // when
        val waitingInfo = getWaitingInfoUseCase.execute(token)

        // then: 대기열에서 2번째에 위치해있다는 대기 정보가 와야한다.
        val expectedPosition = 2
        Assertions.assertThat(waitingInfo.token).isEqualTo(token)
        Assertions.assertThat(waitingInfo.position).isEqualTo(expectedPosition)
        Assertions.assertThat(waitingInfo.estimatedWaitingTime).isEqualTo(expectedPosition * queueProperties.estimatedWorkingTimeInMinutes)
    }

    @Test
    fun `should throw InvalidTokenException when token is not in any queue`() {
        // given: 어떤 Queue 에도 token 이 없는 상황
        val notInQueueToken = "notInQueueToken"

        // when & then
        Assertions
            .assertThatThrownBy { getWaitingInfoUseCase.execute(notInQueueToken) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `should return WaitingInfo that has 0 position and 0 estimatedTime when token is active`() {
        // given: 활성 Queue 에 token 이 있는 상황
        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        redisQueueRepository.addToWaitingQueue(token)
        redisQueueRepository.moveToActiveQueue(1)

        // when
        val waitingInfo = getWaitingInfoUseCase.execute(token)

        // then: 대기열이 아닌 활성 Queue 에 있는 Token 이면, 대기 순번과 예상 대기 시간이 0 이어야 한다.
        Assertions.assertThat(waitingInfo.position).isEqualTo(0)
        Assertions.assertThat(waitingInfo.estimatedWaitingTime).isEqualTo(0)
    }
}
