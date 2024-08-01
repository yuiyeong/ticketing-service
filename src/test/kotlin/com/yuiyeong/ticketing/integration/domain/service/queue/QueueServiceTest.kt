package com.yuiyeong.ticketing.integration.domain.service.queue

import com.yuiyeong.ticketing.config.property.QueueProperties
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.TokenNotInActiveQueueException
import com.yuiyeong.ticketing.domain.repository.queue.QueueRepository
import com.yuiyeong.ticketing.domain.service.queue.QueueService
import com.yuiyeong.ticketing.infrastructure.redis.repository.RedisQueueRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.redisson.Redisson
import org.redisson.config.Config
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test
import kotlin.time.Duration.Companion.nanoseconds

@Testcontainers
class QueueServiceTest {
    private val properties = QueueProperties()

    private lateinit var queueService: QueueService

    private lateinit var queueRepository: QueueRepository

    @BeforeEach
    fun beforeEach() {
        val config =
            Config().apply {
                useSingleServer().address =
                    "redis://${redisContainer.host}:${redisContainer.firstMappedPort}"
            }
        val redissonClient = Redisson.create(config)
        redissonClient.keys.flushall()

        queueRepository = RedisQueueRepository(properties, redissonClient)
        queueService = QueueService(properties, queueRepository)
    }

    @Test
    fun `should throw TokenNotProcessedException when token is not in a active queue`() {
        // given: token 이 대기열에도, 활성 Queue 에도 없는 상황
        val notInQueueToken = "notInQueueToken"

        // when & then
        Assertions
            .assertThatThrownBy { queueService.verifyTokenIsActive(notInQueueToken) }
            .isInstanceOf(TokenNotInActiveQueueException::class.java)
    }

    @Test
    fun `should throw TokenNotProcessedException when token is in a waiting queue`() {
        // given: 대기열에 token 이 있는 상황
        val tokenInWaitingQueue = "tokenInWaitingQueue"
        queueRepository.addToWaitingQueue(tokenInWaitingQueue)

        // when & then
        Assertions
            .assertThatThrownBy { queueService.verifyTokenIsActive(tokenInWaitingQueue) }
            .isInstanceOf(TokenNotInActiveQueueException::class.java)
    }

    @Test
    fun `should return WaitingInfo when token is in the queue`() {
        // given: 대기열에서 token 앞에 1개의 다른 token 이 있는 상황
        queueRepository.addToWaitingQueue("first-waiting-token")

        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        queueRepository.addToWaitingQueue(token)

        // when
        val waitingInfo = queueService.getWaitingInfo(token)

        // then: 대기열에서 2번째에 위치해있다는 대기 정보가 와야한다.
        val expectedPosition = 2
        Assertions.assertThat(waitingInfo).isNotNull()
        Assertions.assertThat(waitingInfo!!.token).isEqualTo(token)
        Assertions.assertThat(waitingInfo.position).isEqualTo(expectedPosition)
        Assertions.assertThat(waitingInfo.estimatedWaitingTime).isEqualTo(expectedPosition * properties.estimatedWorkingTimeInMinutes)
    }

    @Test
    fun `should return null when token is not in waiting queue`() {
        // given: 대기열에 없는 token 이 있는 상황
        val notInQueueToken = "notInQueueToken"

        // when
        val waitingInfo = queueService.getWaitingInfo(notInQueueToken)

        // then: 대기열에 없으므로, 대기 정보가 없어야 한다.
        Assertions.assertThat(waitingInfo).isNull()
    }

    @Test
    fun `should return null when token is active`() {
        // given: 활성 Queue 에 token 이 있는 상황
        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        queueRepository.addToWaitingQueue(token)
        queueRepository.moveToActiveQueue(1)

        // when
        val waitingInfo = queueService.getWaitingInfo(token)

        // then: 대기열에 없으므로, 대기 정보가 없어야한다.
        Assertions.assertThat(waitingInfo).isNull()
    }

    @Test
    fun `should return WaitingInfo after entering the queue`() {
        // given: 대기열에 2개의 token 이 있는 상황
        queueRepository.addToWaitingQueue("first-waiting-token")
        queueRepository.addToWaitingQueue("second-waiting-token")
        val token = "test-token-${System.currentTimeMillis().nanoseconds}"

        // when: 대기열에 진입
        val waitingInfo = queueService.enter(token)

        // then: 3번째로 대기열에 진입했다는 대기 정보가 와야한다.
        val expectedPosition = 3
        Assertions.assertThat(waitingInfo.token).isEqualTo(token)
        Assertions.assertThat(waitingInfo.position).isEqualTo(expectedPosition)
        Assertions.assertThat(waitingInfo.estimatedWaitingTime).isEqualTo(expectedPosition * properties.estimatedWorkingTimeInMinutes)
    }

    @Test
    fun `should return null when getting waiting info after exiting for a token in waiting queue`() {
        // given: 대기열에 token 이 있는 상황
        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        queueRepository.addToWaitingQueue(token)

        // when
        queueService.exit(token)

        // then: 대기열에 없으므로, 대기 정보가 없어야 한다.
        val waitingInfo = queueService.getWaitingInfo(token)
        Assertions.assertThat(waitingInfo).isNull()
    }

    @Test
    fun `should throw TokenNotProcessableException when calling verifyTokenIsActive after exiting for a token in active queue`() {
        // given: 활성 Queue 에 token 이 있는 상황
        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        queueRepository.addToWaitingQueue(token)
        queueRepository.moveToActiveQueue(1)

        // when
        queueService.exit(token)

        // then: 활성 Queue 에 없으므로, TokenNotProcessableException 이 발생해야 한다.
        Assertions
            .assertThatThrownBy { queueService.verifyTokenIsActive(token) }
            .isInstanceOf(TokenNotInActiveQueueException::class.java)
    }

    @Test
    fun `should throw InvalidTokenException when trying to exit for a token not in any queue`() {
        // given: token 이 대기열에도, 활성 Queue 에도 없는 상황
        val notInQueueToken = "notInQueueToken"

        // when & then: 제거할 수 없는 token 이므로, InvalidTokenException 이 발생해야 한다.
        Assertions
            .assertThatThrownBy { queueService.exit(notInQueueToken) }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    @Test
    fun `should return a count for activating tokens in waiting queue`() {
        // given: 3개의 token 이 대기열에 있는 상황
        val tokenCount = 3
        (1..tokenCount).forEachIndexed { _, i ->
            queueRepository.addToWaitingQueue("test-waiting-token-$i")
        }

        // when
        val activatedCount = queueService.activateWaitingEntries()

        // then: 최대 처리 가능한 순번의 token 까지 활성 Queue 로 옯겼을 것이므로,
        //       activatedCount 는 대기열에 있는 총 token 수와 같아야한다.
        Assertions.assertThat(activatedCount).isEqualTo(tokenCount)
    }

    @Test
    fun `should return null after activateWaitingEntries`() {
        // given: 1개의 token 이 대기열에 있는 상황
        val token = "test-token-${System.currentTimeMillis().nanoseconds}"
        queueRepository.addToWaitingQueue(token)

        // when
        queueService.activateWaitingEntries()

        // then: 대기 정보는 없고, 활성 상태여야한다.
        Assertions.assertThat(queueService.getWaitingInfo(token)).isNull()
        queueService.verifyTokenIsActive(token) // exception 이 발생하지 않음
    }

    @Test
    fun `should return zero for activating tokens when there is no token in waiting queue`() {
        // given: 대기열에 아무 token 도 없는 상황

        // when
        val activatedCount = queueService.activateWaitingEntries()

        // then: 아무것도 활성화하지 않았으므로, activatedCount 는 0 이어야 한다.
        Assertions.assertThat(activatedCount).isEqualTo(0)
    }

    companion object {
        @Container
        private val redisContainer =
            GenericContainer(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379)
                .withCommand("redis-server", "--notify-keyspace-events", "KEA") // redis 의 이벤트를 받기 위한 command
    }
}
