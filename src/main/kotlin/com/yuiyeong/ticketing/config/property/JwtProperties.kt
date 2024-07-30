package com.yuiyeong.ticketing.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "config.jwt")
class JwtProperties {
    var secret: String = "some-secret"
}
