package com.yuiyeong.ticketing.infrastructure.redis.repository

import com.yuiyeong.ticketing.config.QueueProperties
import com.yuiyeong.ticketing.domain.repository.queue.QueueRepository
import org.redisson.api.ExpiredObjectListener
import org.redisson.api.RScoredSortedSet
import org.redisson.api.RSet
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.Duration
import kotlin.math.min

@Repository
class RedisQueueRepository(
    queueProperties: QueueProperties,
    private val redissonClient: RedissonClient,
) : QueueRepository {
    private val waitingQueue: RScoredSortedSet<String>
        get() = redissonClient.getScoredSortedSet(KEY_WAITING_QUEUE)

    private val activeQueue: RSet<String>
        get() = redissonClient.getSet(KEY_ACTIVE_QUEUE)

    private val ttl = Duration.ofSeconds(queueProperties.tokenTtlInSeconds)
    private val batchSize = queueProperties.batchSizeToMoveToActive

    private val logger = LoggerFactory.getLogger(RedisQueueRepository::class.java.simpleName)

    override fun addToWaitingQueue(token: String): Boolean {
        val score = System.currentTimeMillis().toDouble()
        val isAdded = waitingQueue.add(score, token)
        if (isAdded) {
            setTokenTtl(token)
        }
        return isAdded
    }

    private fun setTokenTtl(token: String) {
        val tokenTtlTracker = redissonClient.getBucket<String>(token)
        tokenTtlTracker.addListener(
            object : ExpiredObjectListener {
                override fun onExpired(token: String) {
                    removeFromQueue(token)
                    logger.info("Token removed due to expiration;$token")
                }
            },
        )
        tokenTtlTracker.set(token, ttl)
    }

    override fun getWaitingQueuePosition(token: String): Int? {
        val position = waitingQueue.rank(token) ?: return null
        return position + 1
    }

    override fun isInActiveQueue(token: String): Boolean = activeQueue.contains(token)

    override fun getActiveQueueSize(): Int = activeQueue.size

    override fun isInWaitingQueue(token: String): Boolean = waitingQueue.contains(token)

    override fun getWaitingQueueSize(): Int = waitingQueue.size()

    override fun removeFromQueue(token: String): Boolean = waitingQueue.remove(token) || activeQueue.remove(token)

    override fun moveToActiveQueue(countToMove: Int): Int {
        if (countToMove == 0) return 0

        val loopCount = (countToMove + batchSize - 1) / batchSize // 올림 처리해서 반복 횟수 계산
        var remainingCount = countToMove
        var totalMoved = 0

        repeat(loopCount) {
            val batchSize = min(batchSize, remainingCount)

            try {
                val count = moveWaitingQueueToActiveQueueAsBatch(batchSize)

                totalMoved += count
                remainingCount -= count
            } catch (e: Exception) {
                logger.warn("Error on [$loopCount] th moving tokens to active queue.", e)
            }
        }

        return totalMoved
    }

    private fun moveWaitingQueueToActiveQueueAsBatch(batchSize: Int): Int {
        val tokensWithScore = waitingQueue.pollFirstEntries(batchSize)
        if (tokensWithScore.isEmpty()) return 0

        val tokens = tokensWithScore.map { it.value }
        val successToMoveCount = activeQueue.addAllCounted(tokens)

        // active 가 되는데 실패한 token 들 원복
        if (successToMoveCount < tokensWithScore.size) {
            val failedTokens = tokensWithScore.takeLast(tokensWithScore.size - successToMoveCount)
            waitingQueue.addAll(failedTokens.associate { it.value to it.score })
        }

        return successToMoveCount
    }

    companion object {
        const val KEY_WAITING_QUEUE = "concert:waiting_queue"
        const val KEY_ACTIVE_QUEUE = "concert:active_queue"
    }
}
