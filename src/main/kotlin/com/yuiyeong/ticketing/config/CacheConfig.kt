package com.yuiyeong.ticketing.config

import com.yuiyeong.ticketing.config.property.CachingProperties
import org.redisson.api.RedissonClient
import org.redisson.spring.cache.CacheConfig
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableCaching
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
                CacheNames.AVAILABLE_EVENTS to CacheConfig(properties.ttlTenMin, properties.maxIdleTimeHalfTenMin),
                CacheNames.CONCERT_EVENT to CacheConfig(properties.ttlTenMin, properties.maxIdleTimeHalfTenMin),
            )
        return RedissonSpringCacheManager(redissonClient, config)
    }
}

object CacheNames {
    const val CONCERTS = "concerts"
    const val AVAILABLE_EVENTS = "availableEvents"
    const val CONCERT_EVENT = "concertEvent"
}
