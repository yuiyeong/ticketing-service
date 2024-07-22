package com.yuiyeong.ticketing.domain.model.queue

import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExitedException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyProcessingException
import com.yuiyeong.ticketing.domain.exception.QueueEntryOverdueException
import java.time.ZonedDateTime

class QueueEntry(
    val id: Long,
    val userId: Long,
    val token: String,
    position: Long,
    status: QueueEntryStatus,
    val expiresAt: ZonedDateTime,
    val enteredAt: ZonedDateTime,
    processingStartedAt: ZonedDateTime?,
    exitedAt: ZonedDateTime?,
    expiredAt: ZonedDateTime?,
) {
    var position: Long = position
        private set

    var status: QueueEntryStatus = status
        private set

    var processingStartedAt: ZonedDateTime? = processingStartedAt
        private set

    var exitedAt: ZonedDateTime? = exitedAt
        private set

    var expiredAt: ZonedDateTime? = expiredAt
        private set

    /**
     * 상대적 position 을 계산하는 함수
     */
    fun calculateRelativePosition(waitingPositionOffset: Long) = position - waitingPositionOffset

    /**
     * 예상 대기 시간을 계산하는 함수
     */
    fun calculateEstimatedWaitingTime(waitingPositionOffset: Long) = calculateRelativePosition(waitingPositionOffset) * WORKING_MINUTES

    /**
     * 작업 상태로 변경하고 그 일시는 moment 로 설정하는 함수
     */
    fun process(moment: ZonedDateTime) {
        verifyOnWaitingOrProcessing()
        verifyNotOverExpiresAt(moment)

        if (status == QueueEntryStatus.PROCESSING) {
            throw QueueEntryAlreadyProcessingException()
        }

        resetPosition()
        status = QueueEntryStatus.PROCESSING
        processingStartedAt = moment
    }

    /**
     * 대기열에서 나감 상태로 변경하고 그 일시는 moment 로 설정하는 함수
     */
    fun exit(moment: ZonedDateTime) {
        verifyOnWaitingOrProcessing()
        verifyNotOverExpiresAt(moment)

        resetPosition()
        status = QueueEntryStatus.EXITED
        exitedAt = moment
    }

    /**
     * 만료 상태로 변경하고 그 일시는 moment 로 설정하는 함수
     */
    fun expire(moment: ZonedDateTime) {
        verifyOnWaitingOrProcessing()

        resetPosition()
        status = QueueEntryStatus.EXPIRED
        expiredAt = moment
    }

    /**
     * 이 queueEntry 가 대기 혹은 작업 상태임을 검증하는 함수
     */
    private fun verifyOnWaitingOrProcessing() {
        if (status == QueueEntryStatus.EXPIRED) {
            throw QueueEntryAlreadyExpiredException()
        }

        if (status == QueueEntryStatus.EXITED) {
            throw QueueEntryAlreadyExitedException()
        }
    }

    /**
     * moment 가 expiresAt 을 지나지 않았음을 검증하는 함수
     */
    private fun verifyNotOverExpiresAt(moment: ZonedDateTime) {
        // status 가 변경되기 전에 시간이 지난 경우
        if (!moment.isBefore(expiresAt)) {
            throw QueueEntryOverdueException()
        }
    }

    private fun resetPosition() {
        position = 0
    }

    fun copy(
        id: Long = this.id,
        userId: Long = this.userId,
        token: String = this.token,
        position: Long = this.position,
        status: QueueEntryStatus = this.status,
        expiresAt: ZonedDateTime = this.expiresAt,
        enteredAt: ZonedDateTime = this.enteredAt,
        processingStartedAt: ZonedDateTime? = this.processingStartedAt,
        exitedAt: ZonedDateTime? = this.exitedAt,
        expiredAt: ZonedDateTime? = this.expiredAt,
    ): QueueEntry =
        QueueEntry(
            id = id,
            userId = userId,
            token = token,
            position = position,
            status = status,
            expiresAt = expiresAt,
            enteredAt = enteredAt,
            processingStartedAt = processingStartedAt,
            exitedAt = exitedAt,
            expiredAt = expiredAt,
        )

    companion object {
        const val WORKING_MINUTES = 5L // 1명당 5분 동안 작업을 진행한다고 가정

        fun create(
            userId: Long,
            position: Long,
            token: String,
            status: QueueEntryStatus,
            enteredAt: ZonedDateTime,
            expiresAt: ZonedDateTime,
        ): QueueEntry {
            val processingStartedAt = if (status == QueueEntryStatus.PROCESSING) enteredAt else null
            return QueueEntry(
                id = 0L,
                userId = userId,
                token = token,
                position = position,
                status = status,
                expiresAt = expiresAt,
                enteredAt = enteredAt,
                processingStartedAt = processingStartedAt,
                exitedAt = null,
                expiredAt = null,
            )
        }
    }
}

enum class QueueEntryStatus {
    WAITING,
    PROCESSING,
    EXITED,
    EXPIRED,
}
