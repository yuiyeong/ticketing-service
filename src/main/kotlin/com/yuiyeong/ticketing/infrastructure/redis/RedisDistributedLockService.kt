package com.yuiyeong.ticketing.infrastructure.redis

import com.yuiyeong.ticketing.config.DistributedLockProperties
import com.yuiyeong.ticketing.domain.service.lock.DistributedLockService
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisDistributedLockService(
    private val distributedLockProperties: DistributedLockProperties,
    private val distributedLock: RedisDistributedLock,
) : DistributedLockService {
    override fun <T> withLock(
        key: String,
        action: () -> T,
    ): T? {
        if (distributedLock.acquire(key, Duration.ofMillis(distributedLockProperties.lockTtl))) {
            try {
                return action() // lock 획득 후 작업 진행
            } finally {
                distributedLock.release(key) // 락 해제
            }
        }
        return null // 락 획득 실패
    }

    override fun <T> withLockAndRetry(
        key: String,
        action: () -> T,
    ): T? {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + distributedLockProperties.acquireTimeout

        while (System.currentTimeMillis() < endTime) {
            if (distributedLock.acquire(key, Duration.ofMillis(distributedLockProperties.lockTtl))) {
                try {
                    return action() // lock 획득 후 작업 진행
                } finally {
                    distributedLock.release(key) // 락 해제
                }
            }
            Thread.sleep(100) // 짧은 대기 시간 후 재시도
        }

        return null // 락 획득 실패
    }
}
