package com.yuiyeong.ticketing.config

import com.yuiyeong.ticketing.interfaces.api.interceptor.UserTokenInterceptor
import com.yuiyeong.ticketing.interfaces.api.resolver.CurrentTokenArgumentResolver
import com.yuiyeong.ticketing.interfaces.api.resolver.CurrentUserIdArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val userTokenInterceptor: UserTokenInterceptor,
    private val currentUserIdArgumentResolver: CurrentUserIdArgumentResolver,
    private val currentTokenArgumentResolver: CurrentTokenArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(userTokenInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserIdArgumentResolver)
        resolvers.add(currentTokenArgumentResolver)
    }
}
