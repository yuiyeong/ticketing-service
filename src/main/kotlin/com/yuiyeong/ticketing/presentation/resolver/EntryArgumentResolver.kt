package com.yuiyeong.ticketing.presentation.resolver

import com.yuiyeong.ticketing.application.annotation.CurrentEntry
import com.yuiyeong.ticketing.application.dto.queue.QueueEntryResult
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.presentation.interceptor.UserTokenInterceptor
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class EntryArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentEntry::class.java) && parameter.parameterType == QueueEntryResult::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
        return request?.getAttribute(UserTokenInterceptor.ATTR_QUEUE_ENTRY) as? QueueEntryResult
            ?: throw InvalidTokenException()
    }
}
