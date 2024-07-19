package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.QueueEntry
import com.yuiyeong.ticketing.domain.model.QueueEntryStatus

data class QueueEntryResult(
    val id: Long,
    val userId: Long,
    val token: String,
    val position: Long,
    val status: QueueEntryStatus,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(
            entry: QueueEntry,
            waitingPositionOffset: Long = 0,
        ) = QueueEntryResult(
            id = entry.id,
            userId = entry.userId,
            token = entry.token,
            position = entry.calculateRelativePosition(waitingPositionOffset),
            status = entry.status,
            estimatedWaitingTime = entry.calculateEstimatedWaitingTime(waitingPositionOffset),
        )
    }
}
