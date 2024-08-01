package com.yuiyeong.ticketing.unit.domain.service.queue

import com.yuiyeong.ticketing.config.property.QueueProperties
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.QueueNotAvailableException
import com.yuiyeong.ticketing.domain.exception.TokenNotInActiveQueueException
import com.yuiyeong.ticketing.domain.repository.queue.QueueRepository
import com.yuiyeong.ticketing.domain.service.queue.QueueService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.BDDMockito.verify
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class QueueServiceTest {
    private val queueProperties: QueueProperties = QueueProperties()

    @Mock
    private lateinit var queueRepository: QueueRepository

    private lateinit var queueService: QueueService

    @BeforeEach
    fun setup() {
        queueService = QueueService(queueProperties, queueRepository)
    }

    @Test
    fun `should return WaitingInfo when token is in the queue`() {
        // given
        val token = "test-token"
        val position = 2
        given(queueRepository.getWaitingQueuePosition(token)).willReturn(position)

        // when
        val waitingInfo = queueService.getWaitingInfo(token)

        // then
        Assertions.assertThat(waitingInfo).isNotNull
        Assertions.assertThat(waitingInfo!!.token).isEqualTo(token)
        Assertions.assertThat(waitingInfo.position).isEqualTo(position)
        Assertions
            .assertThat(waitingInfo.estimatedWaitingTime)
            .isEqualTo(position * queueProperties.estimatedWorkingTimeInMinutes)

        verify(queueRepository).getWaitingQueuePosition(token)
    }

    @Test
    fun `should return WaitingInfo after entering the queue`() {
        // given
        val token = "test-token"
        val position = 3
        given(queueRepository.addToWaitingQueue(token)).willReturn(true)
        given(queueRepository.getWaitingQueuePosition(token)).willReturn(position)

        // when
        val waitingInfo = queueService.enter(token)

        // then
        Assertions.assertThat(waitingInfo.token).isEqualTo(token)
        Assertions.assertThat(waitingInfo.position).isEqualTo(position)
        Assertions
            .assertThat(waitingInfo.estimatedWaitingTime)
            .isEqualTo(position * queueProperties.estimatedWorkingTimeInMinutes)

        verify(queueRepository).addToWaitingQueue(token)
        verify(queueRepository).getWaitingQueuePosition(token)
    }

    @Test
    fun `should throw InvalidTokenException when trying to exit for a token not in any queue`() {
        // given
        val notInQueueToken = "notInQueueToken"
        given(queueRepository.isInActiveQueue(notInQueueToken)).willReturn(false)
        given(queueRepository.isInWaitingQueue(notInQueueToken)).willReturn(false)

        // when & then
        Assertions
            .assertThatThrownBy { queueService.exit(notInQueueToken) }
            .isInstanceOf(InvalidTokenException::class.java)

        verify(queueRepository).isInActiveQueue(notInQueueToken)
        verify(queueRepository).isInWaitingQueue(notInQueueToken)
    }

    @Test
    fun `should return a count for activating tokens in waiting queue`() {
        // given
        val tokenCount = 3
        given(queueRepository.getWaitingQueueSize()).willReturn(tokenCount)
        given(queueRepository.moveToActiveQueue(tokenCount)).willReturn(tokenCount)

        // when
        val activatedCount = queueService.activateWaitingEntries()

        // then
        Assertions.assertThat(activatedCount).isEqualTo(tokenCount)

        verify(queueRepository).getWaitingQueueSize()
        verify(queueRepository).moveToActiveQueue(tokenCount)
    }

    @Test
    fun `should throw InvalidTokenException when token is not in any queue`() {
        // given
        val token = "invalidToken"
        given(queueRepository.isInActiveQueue(token)).willReturn(false)
        given(queueRepository.isInWaitingQueue(token)).willReturn(false)

        // when & then
        Assertions
            .assertThatThrownBy { queueService.verifyTokenIsInAnyQueue(token) }
            .isInstanceOf(InvalidTokenException::class.java)

        verify(queueRepository).isInActiveQueue(token)
        verify(queueRepository).isInWaitingQueue(token)
    }

    @Test
    fun `should throw TokenNotInActiveQueueException when token is not in active queue`() {
        // given
        val token = "inactiveToken"
        given(queueRepository.isInActiveQueue(token)).willReturn(false)

        // when & then
        Assertions
            .assertThatThrownBy { queueService.verifyTokenIsActive(token) }
            .isInstanceOf(TokenNotInActiveQueueException::class.java)

        verify(queueRepository).isInActiveQueue(token)
    }

    @Test
    fun `should return null when token is not in waiting queue`() {
        // given: 대기열에 token 이 없는 상황
        val token = "notWaitingToken"
        given(queueRepository.getWaitingQueuePosition(token)).willReturn(null)

        // when
        val waitingInfo = queueService.getWaitingInfo(token)

        // then
        Assertions.assertThat(waitingInfo).isNull()

        verify(queueRepository).getWaitingQueuePosition(token)
    }

    @Test
    fun `should return WaitingInfo when token is in waiting queue`() {
        // given: token 이 대기열의 5번째에 있는 상황
        val token = "waitingToken"
        val position = 5
        given(queueRepository.getWaitingQueuePosition(token)).willReturn(position)

        // when
        val waitingInfo = queueService.getWaitingInfo(token)

        // then: 5번째인 대기 정보가 와야한다.
        Assertions.assertThat(waitingInfo).isNotNull
        Assertions.assertThat(waitingInfo!!.token).isEqualTo(token)
        Assertions.assertThat(waitingInfo.position).isEqualTo(position)
        Assertions
            .assertThat(waitingInfo.estimatedWaitingTime)
            .isEqualTo(position * queueProperties.estimatedWorkingTimeInMinutes)

        verify(queueRepository).getWaitingQueuePosition(token)
    }

    @Test
    fun `should throw QueueNotAvailableException when adding is not possible`() {
        // given: 대기열에 token 을 넣을 수 없는 상황
        val token = "newToken"
        given(queueRepository.addToWaitingQueue(token)).willReturn(false)

        // when & then: 대기열에 없으므로, 대기 정보가 없어야 한다.
        Assertions
            .assertThatThrownBy { queueService.enter(token) }
            .isInstanceOf(QueueNotAvailableException::class.java)

        verify(queueRepository).addToWaitingQueue(token)
    }

    @Test
    fun `should return WaitingInfo when successfully added to queue`() {
        // given: 대기열에 9개의 token 있는 상황
        val token = "newToken"
        val position = 10
        given(queueRepository.addToWaitingQueue(token)).willReturn(true)
        given(queueRepository.getWaitingQueuePosition(token)).willReturn(position)

        // when
        val waitingInfo = queueService.enter(token)

        // then: 10번째로 대기열에 진입했다는 대기 정보가 와야한다.
        Assertions.assertThat(waitingInfo.token).isEqualTo(token)
        Assertions.assertThat(waitingInfo.position).isEqualTo(position)
        Assertions
            .assertThat(waitingInfo.estimatedWaitingTime)
            .isEqualTo(position * queueProperties.estimatedWorkingTimeInMinutes)

        verify(queueRepository).addToWaitingQueue(token)
        verify(queueRepository).getWaitingQueuePosition(token)
    }

    @Test
    fun `should remove token from queue when token is valid`() {
        // given: token 이 활성 Queue 에 있는 상황
        val token = "validToken"
        given(queueRepository.isInActiveQueue(token)).willReturn(true)

        // when
        queueService.exit(token)

        // then
        verify(queueRepository).removeFromQueue(token)
    }

    @Test
    fun `should return zero when waiting queue is empty`() {
        // given: 대기열에 아무 token 도 없는 상황
        val movedCount = 0
        given(queueRepository.getWaitingQueueSize()).willReturn(movedCount)

        // when
        val result = queueService.activateWaitingEntries()

        // then: 아무것도 활성화하지 않았으므로, activatedCount 는 0 이어야 한다.
        Assertions.assertThat(result).isEqualTo(0)

        verify(queueRepository).getWaitingQueueSize()
        verify(queueRepository, never()).moveToActiveQueue(movedCount)
    }

    @Test
    fun `should move entries to active queue`() {
        // given: 대기열에 2000 개의 token 이 있고, 그 2000 개를 활성 Queue 로 옮기려는 상황
        val waitingQueueSize = 2000
        given(queueRepository.getWaitingQueueSize()).willReturn(waitingQueueSize)
        given(queueRepository.moveToActiveQueue(waitingQueueSize)).willReturn(waitingQueueSize)

        // when
        val result = queueService.activateWaitingEntries()

        // then
        Assertions.assertThat(result).isEqualTo(waitingQueueSize)

        verify(queueRepository).getWaitingQueueSize()
        verify(queueRepository).moveToActiveQueue(waitingQueueSize)
    }
}
