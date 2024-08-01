package com.yuiyeong.ticketing.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfig(
    private val redisProperties: RedisProperties,
) {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        return template
    }

    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate {
        val template = StringRedisTemplate()
        template.connectionFactory = redisConnectionFactory
        return template
    }

    @Bean
    fun redissonConnectionFactory(redissonClient: RedissonClient): RedissonConnectionFactory = RedissonConnectionFactory(redissonClient)

    @Bean
    fun redissonClient(): RedissonClient {
        val config =
            Config().also {
                it
                    .useSingleServer()
                    .setAddress("redis://${redisProperties.host}:${redisProperties.port}")
            }

        return Redisson.create(config)
    }
}
