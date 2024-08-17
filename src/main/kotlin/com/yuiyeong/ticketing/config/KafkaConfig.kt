package com.yuiyeong.ticketing.config

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@EnableKafka
@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
    private val sslBundles: SslBundles,
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> = DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties(sslBundles))

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val props = kafkaProperties.buildConsumerProperties(sslBundles)
        props[JsonDeserializer.TRUSTED_PACKAGES] = "com.yuiyeong.ticketing.*"
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}
