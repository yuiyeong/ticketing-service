package com.yuiyeong.ticketing.domain.lock

import java.time.Duration

interface DistributedLock {
    fun acquire(
        key: String,
        timeout: Duration,
    ): Boolean

    fun release(key: String)
}
