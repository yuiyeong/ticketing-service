package com.yuiyeong.ticketing.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "logging.request-response")
class LoggingProperties {
    var enabled: Boolean = false
    var previewLength: Int = 200
    var maxContentLength: Int = 524288 // 500 KB
}
