package com.yuiyeong.ticketing.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "config.jwt")
class JwtProperties {
    var secret: String = "test_very_difficult_and_secure_secret_key_that_is_at_least_256_bits"
}
