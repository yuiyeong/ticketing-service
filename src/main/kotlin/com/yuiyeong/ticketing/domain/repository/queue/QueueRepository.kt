package com.yuiyeong.ticketing.domain.repository.queue

interface QueueRepository {
    fun addToWaitingQueue(token: String): Boolean

    fun getWaitingQueuePosition(token: String): Int?

    fun isInActiveQueue(token: String): Boolean

    fun getActiveQueueSize(): Int

    fun isInWaitingQueue(token: String): Boolean

    fun getWaitingQueueSize(): Int

    fun removeFromQueue(token: String): Boolean

    fun moveToActiveQueue(countToMove: Int): Int
}
