package com.yuiyeong.ticketing.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SlackProperties::class)
class SlackConfig

@ConfigurationProperties(prefix = "slack")
data class SlackProperties(
    val webhookUrl: String,
)
