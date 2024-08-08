package com.yuiyeong.ticketing.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "slack")
class SlackProperties {
    var webhookUrl = ""
}
