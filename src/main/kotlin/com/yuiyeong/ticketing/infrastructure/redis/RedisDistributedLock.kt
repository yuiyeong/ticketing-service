package com.yuiyeong.ticketing.infrastructure.redis

import com.yuiyeong.ticketing.domain.lock.DistributedLock
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisDistributedLock(
    private val redisTemplate: RedisTemplate<String, Any>,
) : DistributedLock {
    override fun acquire(
        key: String,
        timeout: Duration,
    ): Boolean =
        redisTemplate
            .opsForValue()
            .setIfAbsent(key, "locked", timeout) ?: false

    override fun release(key: String) {
        redisTemplate.delete(key)
    }
}
