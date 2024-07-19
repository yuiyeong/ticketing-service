package com.yuiyeong.ticketing.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "config.scheduler")
class SchedulerProperties {
    var queueFixedRate: Long = 60000 // 1분
    var occupationFixedRate: Long = 60000 // 1분
}
