package com.yuiyeong.ticketing.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig

@ConfigurationProperties(prefix = "config.jwt")
data class JwtProperties(
    val secret: String,
)
