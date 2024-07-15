package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExitedException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyExpiredException
import com.yuiyeong.ticketing.domain.exception.QueueEntryAlreadyProcessingException
import com.yuiyeong.ticketing.domain.exception.QueueEntryOverdueException
import com.yuiyeong.ticketing.domain.exception.TokenNotProcessableException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Base64

data class WaitingEntry(
    val id: Long,
    val userId: Long,
    val token: String,
    var position: Long,
    var status: WaitingEntryStatus,
    val expiresAt: ZonedDateTime,
    val enteredAt: ZonedDateTime,
    var processingStartedAt: ZonedDateTime?,
    var exitedAt: ZonedDateTime?,
    var expiredAt: ZonedDateTime?,
) {
    /**
     * 예상 대기 시간을 계산하는 함수
     */
    fun calculateEstimatedWaitingTime(waitingPositionOffset: Long): Long = (position - waitingPositionOffset) * WORKING_MINUTES

    /**
     * 이 WaitingEntry 가 작업 상태임을 검증하는 함수
     */
    fun verifyOnProcessing() {
        if (status != WaitingEntryStatus.PROCESSING) {
            throw TokenNotProcessableException()
        }
    }

    /**
     * 작업 상태로 변경하고 그 일시는 moment 로 설정하는 함수
     */
    fun process(moment: ZonedDateTime) {
        verifyOnWaitingOrProcessing()
        verifyNotOverExpiresAt(moment)

        if (status == WaitingEntryStatus.PROCESSING) {
            throw QueueEntryAlreadyProcessingException()
        }

        resetPosition()
        status = WaitingEntryStatus.PROCESSING
        processingStartedAt = moment
    }

    /**
     * 대기열에서 나감 상태로 변경하고 그 일시는 moment 로 설정하는 함수
     */
    fun exit(moment: ZonedDateTime) {
        verifyOnWaitingOrProcessing()
        verifyNotOverExpiresAt(moment)

        resetPosition()
        status = WaitingEntryStatus.EXITED
        exitedAt = moment
    }

    /**
     * 만료 상태로 변경하고 그 일시는 moment 로 설정하는 함수
     */
    fun expire(moment: ZonedDateTime) {
        verifyOnWaitingOrProcessing()

        resetPosition()
        status = WaitingEntryStatus.EXPIRED
        expiredAt = moment
    }

    /**
     * 이 WaitingEntry 가 대기 혹은 작업 상태임을 검증하는 함수
     */
    private fun verifyOnWaitingOrProcessing() {
        if (status == WaitingEntryStatus.EXPIRED) {
            throw QueueEntryAlreadyExpiredException()
        }

        if (status == WaitingEntryStatus.EXITED) {
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

    companion object {
        const val WORKING_MINUTES = 5L // 1명당 5분 동안 작업을 진행한다고 가정
        const val EXPIRATION_MINUTES = 60L // 60 분 뒤 만료

        fun create(
            userId: Long,
            position: Long,
            status: WaitingEntryStatus,
        ): WaitingEntry {
            val now = ZonedDateTime.now()
            val expiresAt = now.plusMinutes(EXPIRATION_MINUTES)
            val processingStartedAt = if (status == WaitingEntryStatus.PROCESSING) now else null
            return WaitingEntry(
                id = 0L,
                userId = userId,
                token = generateToken(userId, position, expiresAt),
                position = position,
                status = status,
                expiresAt = expiresAt,
                enteredAt = now,
                processingStartedAt = processingStartedAt,
                exitedAt = null,
                expiredAt = null,
            )
        }

        /**
         * Base64 형식으로 사용자의 id, 대기열 순서, 만료시간을 인코딩한다.
         */
        fun generateToken(
            userId: Long,
            position: Long,
            expiresAt: ZonedDateTime,
        ): String {
            val tokenData = TokenData(userId, position, expiresAt)
            return Base64.getUrlEncoder().encodeToString(tokenData.toByteArray())
        }

        /**
         * token 을 decoding 하여, TokenData 로 만들어준다.
         * decoding 할 수 없을 경우, InvalidTokenException 이 발생한다.
         */
        fun decodeToken(token: String): TokenData {
            try {
                val decoded = Base64.getUrlDecoder().decode(token).toString()
                val (userId, position, expirationTimestamp) = decoded.split(":")
                return TokenData(
                    userId.toLong(),
                    position.toLong(),
                    ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(expirationTimestamp.toLong()),
                        ZoneId.systemDefault(),
                    ),
                )
            } catch (e: Exception) {
                throw InvalidTokenException()
            }
        }
    }
}

enum class WaitingEntryStatus {
    WAITING,
    PROCESSING,
    EXITED,
    EXPIRED,
}

data class TokenData(
    val userId: Long,
    val position: Long,
    val expiresAt: ZonedDateTime,
) {
    fun toByteArray(): ByteArray = toString().toByteArray()

    override fun toString(): String {
        val expirationTimestamp = expiresAt.toEpochSecond()
        return "$userId:$position:$expirationTimestamp"
    }
}
