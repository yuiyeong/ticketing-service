package com.yuiyeong.ticketing.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DistributedLockProperties::class)
class LockConfig

@ConfigurationProperties(prefix = "config.distributed-lock")
data class DistributedLockProperties(
    val acquireTimeout: Long,
    val lockTtl: Long,
)
