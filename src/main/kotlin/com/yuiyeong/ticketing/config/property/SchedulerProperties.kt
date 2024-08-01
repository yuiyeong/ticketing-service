package com.yuiyeong.ticketing.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "config.scheduler")
class SchedulerProperties {
    var enabled: Boolean = true
    var occupationFixedRate: Long = 60000 // 1분
}
