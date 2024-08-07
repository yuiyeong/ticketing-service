package com.yuiyeong.ticketing.domain.service.queue

import com.yuiyeong.ticketing.config.property.QueueProperties
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.QueueNotAvailableException
import com.yuiyeong.ticketing.domain.exception.TokenNotInActiveQueueException
import com.yuiyeong.ticketing.domain.model.queue.WaitingInfo
import com.yuiyeong.ticketing.domain.repository.queue.QueueRepository
import org.springframework.stereotype.Service
import kotlin.math.ceil
import kotlin.math.min

@Service
class QueueService(
    private val queueProperties: QueueProperties,
    private val queueRepository: QueueRepository,
) {
    /**
     * [token] 이 대기열 또는 활성 Queue 에 있는지 검증
     * @throws InvalidTokenException
     */
    fun verifyTokenIsInAnyQueue(token: String) {
        if (!queueRepository.isInActiveQueue(token) && !queueRepository.isInWaitingQueue(token)) {
            throw InvalidTokenException()
        }
    }

    /**
     * [token] 이 활성 Queue 에 있는 token 인지 검증
     * @throws TokenNotInActiveQueueException
     */
    fun verifyTokenIsActive(token: String) {
        if (!queueRepository.isInActiveQueue(token)) {
            throw TokenNotInActiveQueueException()
        }
    }

    /**
     * [token] 이 대기열에 있다면, 의 대기 정보를 반환하고 대기열에 없다면 null 을 반환
     * @return [WaitingInfo] 대기 정보
     */
    fun getWaitingInfo(token: String): WaitingInfo? {
        val position = queueRepository.getWaitingQueuePosition(token) ?: return null
        return WaitingInfo(
            token = token,
            position = position,
            estimatedWaitingTime = calculateEstimatedWaitingTime(position),
        )
    }

    /**
     * [token] 을 대기열에 추가한다.
     * @return [WaitingInfo] 추가된 token 의 대기 정보
     */
    fun enter(token: String): WaitingInfo {
        val isAdded = queueRepository.addToWaitingQueue(token)
        if (!isAdded) throw QueueNotAvailableException()
        val position = queueRepository.getWaitingQueuePosition(token) ?: 0
        return WaitingInfo(
            token = token,
            position = position,
            estimatedWaitingTime = calculateEstimatedWaitingTime(position),
        )
    }

    /**
     * [token] 을 대기열 또는 활성 Queue 에서 제거한다.
     * @throws [InvalidTokenException] 대기열 또는 활성 Queue 에 없는 token 으로 시도한 경우
     */
    fun exit(token: String) {
        verifyTokenIsInAnyQueue(token)

        queueRepository.removeFromQueue(token)
    }

    /**
     * 대기열에 있는 token 들 중, 최대 처리 가능한 순번의 token 까지 활성 Queue 로 옮기는 함수
     */
    fun activateWaitingEntries(): Int {
        val waitingQueueSize = queueRepository.getWaitingQueueSize()
        if (waitingQueueSize == 0) return 0

        val countToMove = min(waitingQueueSize, queueProperties.maxCountToMove)
        return queueRepository.moveToActiveQueue(countToMove)
    }

    /**
     * [position] 번째에 대하여, 예상 대기 시간(초)을 계산해주는 함수
     */
    private fun calculateEstimatedWaitingTime(position: Int): Long {
        val unitSeconds = queueProperties.activeRate / 1000L
        return ceil(position.toDouble() / queueProperties.maxCountToMove.toDouble()).toLong() * unitSeconds
    }
}
