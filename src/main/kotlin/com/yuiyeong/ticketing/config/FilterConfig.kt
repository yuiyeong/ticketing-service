package com.yuiyeong.ticketing.config

import com.yuiyeong.ticketing.config.property.LoggingProperties
import com.yuiyeong.ticketing.presentation.filter.TicketingLoggingFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig(
    private val loggingProperties: LoggingProperties,
) {
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
