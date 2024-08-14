package com.yuiyeong.ticketing.config

import com.yuiyeong.ticketing.interfaces.api.filter.TicketingLoggingFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(LoggingProperties::class)
@Configuration
class LoggingFilterConfig {
    @Bean
    @ConditionalOnBean(TicketingLoggingFilter::class)
    fun loggingFilter(ticketingLoggingFilter: TicketingLoggingFilter): FilterRegistrationBean<TicketingLoggingFilter> {
        val registrationBean = FilterRegistrationBean<TicketingLoggingFilter>()
        registrationBean.filter = ticketingLoggingFilter
        registrationBean.addUrlPatterns("/*")
        registrationBean.order = 1
        return registrationBean
    }
}

@ConfigurationProperties(prefix = "logging.request-response")
data class LoggingProperties(
    var enabled: Boolean,
    var previewLength: Int,
    var maxContentLength: Int,
)
