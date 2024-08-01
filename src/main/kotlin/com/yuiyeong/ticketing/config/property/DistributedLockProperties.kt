package com.yuiyeong.ticketing.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "config.distributed-lock")
class DistributedLockProperties {
    var acquireTimeout = 5000L // 5 초
    var lockTtl = 10000L // 10 초
}
