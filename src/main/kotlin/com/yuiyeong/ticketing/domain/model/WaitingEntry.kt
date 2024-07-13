package com.yuiyeong.ticketing.domain.model

import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.InvalidTokenStatusException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Base64
import java.util.Objects

class WaitingEntry(
    id: Long,
    userId: Long,
    token: String,
    position: Long,
    status: WaitingEntryStatus,
    expiresAt: ZonedDateTime,
    enteredAt: ZonedDateTime,
    processingStartedAt: ZonedDateTime?,
    exitedAt: ZonedDateTime?,
) {
    var id: Long = id
        private set

    var userId: Long = userId
        private set

    var token: String = token
        private set

    var position: Long = position
        private set

    var status: WaitingEntryStatus = status
        private set

    var expiresAt: ZonedDateTime = expiresAt
        private set

    var enteredAt: ZonedDateTime = enteredAt
        private set

    var processingStartedAt: ZonedDateTime? = processingStartedAt
        private set

    var exitedAt: ZonedDateTime? = exitedAt
        private set

    fun process() {
        if (status != WaitingEntryStatus.WAITING) {
            throw IllegalStateException("대기 상태일 때만 작업 중으로 변경가능합니다.")
        }

        status = WaitingEntryStatus.PROCESSING
        position = 0
        processingStartedAt = ZonedDateTime.now()
    }

    fun exit() {
        if (status != WaitingEntryStatus.PROCESSING && status != WaitingEntryStatus.WAITING) {
            throw IllegalStateException("작업 중이거나 대기 중일 때만 대기열에서 제거할 수 있습니다.")
        }

        status = WaitingEntryStatus.EXITED
        position = 0
        exitedAt = ZonedDateTime.now()
    }

    fun expire(current: ZonedDateTime) {
        if (status != WaitingEntryStatus.PROCESSING && status != WaitingEntryStatus.WAITING) {
            throw IllegalStateException("작업 중이거나 대기 중일 때만 만료할 수 있습니다.")
        }

        if (current.isBefore(expiresAt)) {
            throw IllegalStateException("현재 만료 일시가 지나지 않았습니다.")
        }

        status = WaitingEntryStatus.EXPIRED
        position = 0
    }

    fun calculateEstimatedWaitingTime(waitingPositionOffset: Long): Long = (position - waitingPositionOffset) * WORKING_MINUTES

    fun verifyOnProcessing() {
        if (status != WaitingEntryStatus.PROCESSING) {
            throw InvalidTokenStatusException()
        }
    }

    fun copy(
        id: Long = this.id,
        userId: Long = this.userId,
        token: String = this.token,
        position: Long = this.position,
        status: WaitingEntryStatus = this.status,
        expiresAt: ZonedDateTime = this.expiresAt,
        enteredAt: ZonedDateTime = this.enteredAt,
        processingStartedAt: ZonedDateTime? = this.processingStartedAt,
        exitedAt: ZonedDateTime? = this.exitedAt,
    ) = WaitingEntry(
        id,
        userId,
        token,
        position,
        status,
        expiresAt,
        enteredAt,
        processingStartedAt,
        exitedAt,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WaitingEntry

        return id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

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
            val enteredAt = if (status == WaitingEntryStatus.PROCESSING) now else null
            return WaitingEntry(
                id = 0L,
                userId = userId,
                token = generateToken(userId, position, expiresAt),
                position = position,
                status = status,
                expiresAt = expiresAt,
                enteredAt = now,
                processingStartedAt = enteredAt,
                exitedAt = null,
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
