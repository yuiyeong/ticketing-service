package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.domain.model.WaitingEntry
import com.yuiyeong.ticketing.domain.model.WaitingEntryStatus

data class WaitingEntryResult(
    val userId: Long,
    val token: String,
    val position: Long,
    val status: WaitingEntryStatus,
    val estimatedWaitingTime: Long,
) {
    companion object {
        fun from(
            entry: WaitingEntry,
            waitingPositionOffset: Long = 0,
        ) = WaitingEntryResult(
            entry.userId,
            entry.token,
            entry.position,
            entry.status,
            entry.calculateEstimatedWaitingTime(waitingPositionOffset),
        )
    }
}
