package com.yuiyeong.ticketing.domain.service.lock

interface DistributedLockService {
    /**
     * Lock 획득 시도 후, 획득에 성공하면 action 후 결과를 반환. 실패하면 null 반환
     */
    fun <T> withLock(
        key: String,
        action: () -> T,
    ): T?

    /**
     * Lock 획득 할 때까지 기다려서, action 후 결과를 반환.
     * 최종적으로 못 얻으면 null 반환.
     */
    fun <T> withLockByWaiting(
        key: String,
        action: () -> T,
    ): T?
}
