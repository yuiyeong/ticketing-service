package com.yuiyeong.ticketing.domain.service.lock

interface DistributedLockService {
    /**
     * simple lock 방식으로 구현된 방식으로 분산락을 획득 후 action 진행
     */
    fun <T> withLock(
        key: String,
        action: () -> T,
    ): T?

    /**
     * spin lock 방식으로 구현된 방식으로, 분산락을 일징 시간 동안 재시도를 통해 획득 후 action 진행
     */
    fun <T> withLockAndRetry(
        key: String,
        action: () -> T,
    ): T?
}
