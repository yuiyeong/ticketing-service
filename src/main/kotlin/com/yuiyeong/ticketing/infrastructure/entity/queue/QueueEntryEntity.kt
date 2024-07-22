package com.yuiyeong.ticketing.infrastructure.entity.queue

import com.yuiyeong.ticketing.domain.model.queue.QueueEntry
import com.yuiyeong.ticketing.domain.model.queue.QueueEntryStatus
import com.yuiyeong.ticketing.infrastructure.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.ZonedDateTime

@Entity
@Table(name = "user_queue")
class QueueEntryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userId: Long,
    val token: String,
    val queuePosition: Long,
    val status: QueueEntryEntityStatus,
    val expiresAt: ZonedDateTime,
    val processedAt: ZonedDateTime?,
    val exitedAt: ZonedDateTime?,
    val expiredAt: ZonedDateTime?,
) : BaseEntity() {
    fun toQueueEntry(): QueueEntry =
        QueueEntry(
            id = id,
            userId = userId,
            token = token,
            position = queuePosition,
            status = status.toQueueEntryStatus(),
            enteredAt = createdAt,
            expiresAt = expiresAt,
            processingStartedAt = processedAt,
            exitedAt = exitedAt,
            expiredAt = expiredAt,
        )

    companion object {
        fun from(queueEntry: QueueEntry): QueueEntryEntity =
            QueueEntryEntity(
                id = queueEntry.id,
                userId = queueEntry.userId,
                token = queueEntry.token,
                queuePosition = queueEntry.position,
                status = QueueEntryEntityStatus.from(queueEntry.status),
                expiresAt = queueEntry.expiresAt,
                processedAt = queueEntry.processingStartedAt,
                exitedAt = queueEntry.exitedAt,
                expiredAt = queueEntry.expiredAt,
            )
    }
}

enum class QueueEntryEntityStatus {
    READY,
    PROCESSING,
    EXITED,
    EXPIRED,
    ;

    fun toQueueEntryStatus(): QueueEntryStatus =
        when (this) {
            READY -> QueueEntryStatus.WAITING
            PROCESSING -> QueueEntryStatus.PROCESSING
            EXITED -> QueueEntryStatus.EXITED
            EXPIRED -> QueueEntryStatus.EXPIRED
        }

    companion object {
        fun from(queueEntryStatus: QueueEntryStatus): QueueEntryEntityStatus =
            when (queueEntryStatus) {
                QueueEntryStatus.WAITING -> READY
                QueueEntryStatus.PROCESSING -> PROCESSING
                QueueEntryStatus.EXITED -> EXITED
                QueueEntryStatus.EXPIRED -> EXPIRED
            }
    }
}
