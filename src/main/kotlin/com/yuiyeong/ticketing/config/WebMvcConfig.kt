package com.yuiyeong.ticketing.config

import com.yuiyeong.ticketing.application.usecase.token.ValidateTokenUseCase
import com.yuiyeong.ticketing.presentation.interceptor.UserTokenInterceptor
import com.yuiyeong.ticketing.presentation.resolver.EntryArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val entryArgumentResolver: EntryArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(UserTokenInterceptor(validateTokenUseCase))
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(entryArgumentResolver)
    }
}
