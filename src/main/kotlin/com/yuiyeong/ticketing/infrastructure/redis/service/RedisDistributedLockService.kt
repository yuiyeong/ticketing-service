package com.yuiyeong.ticketing.infrastructure.redis.service

import com.yuiyeong.ticketing.config.property.DistributedLockProperties
import com.yuiyeong.ticketing.domain.service.lock.DistributedLockService
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisDistributedLockService(
    private val distributedLockProperties: DistributedLockProperties,
    private val redissonClient: RedissonClient,
) : DistributedLockService {
    override fun <T> withLock(
        key: String,
        action: () -> T,
    ): T? {
        val lock = redissonClient.getLock(key)
        return if (lock.tryLock()) {
            try {
                action()
            } finally {
                lock.unlock()
            }
        } else {
            null
        }
    }

    override fun <T> withLockByWaiting(
        key: String,
        action: () -> T,
    ): T? {
        val lock = redissonClient.getFairLock(key)
        return if (lock.tryLock(distributedLockProperties.acquireTimeout, distributedLockProperties.lockTtl, TimeUnit.MILLISECONDS)) {
            try {
                action()
            } finally {
                lock.unlock()
            }
        } else {
            null
        }
    }
}
