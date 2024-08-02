package com.yuiyeong.ticketing.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "config.caching")
class CachingProperties {
    var ttlHour: Long = 60 * 60 * 1000L // 1시간
    var maxIdleTimeHalfHour: Long = 30 * 60 * 1000L // 30분
    var ttlTenMin: Long = 10 * 60 * 1000L // 10분
    var maxIdleTimeHalfTenMin: Long = 5 * 60 * 1000L // 5분
}
