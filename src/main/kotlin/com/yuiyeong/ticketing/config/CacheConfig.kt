package com.yuiyeong.ticketing.config

import org.redisson.api.RedissonClient
import org.redisson.spring.cache.CacheConfig
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableCaching
@EnableConfigurationProperties(CachingProperties::class)
@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(
        redissonClient: RedissonClient,
        properties: CachingProperties,
    ): CacheManager {
        val config: Map<String, CacheConfig> =
            mapOf(
                CacheNames.CONCERTS to CacheConfig(properties.ttlHour, properties.maxIdleTimeHalfHour),
                CacheNames.AVAILABLE_CONCERT_EVENTS to
                    CacheConfig(
                        properties.ttlTenMin,
                        properties.maxIdleTimeHalfTenMin,
                    ),
            )
        return RedissonSpringCacheManager(redissonClient, config)
    }
}

@ConfigurationProperties(prefix = "config.caching")
data class CachingProperties(
    val ttlHour: Long,
    val maxIdleTimeHalfHour: Long,
    val ttlTenMin: Long,
    val maxIdleTimeHalfTenMin: Long,
)

object CacheNames {
    const val CONCERTS = "concerts"
    const val AVAILABLE_CONCERT_EVENTS = "availableConcertEvents"
}
