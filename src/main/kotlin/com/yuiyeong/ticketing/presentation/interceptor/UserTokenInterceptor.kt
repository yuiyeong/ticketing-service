package com.yuiyeong.ticketing.presentation.interceptor

import com.yuiyeong.ticketing.application.annotation.RequiresUserToken
import com.yuiyeong.ticketing.application.usecase.token.ValidateTokenUseCase
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.TokenNotProcessableException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class UserTokenInterceptor(
    private val validateTokenUseCase: ValidateTokenUseCase,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod) {
            val requiresUserToken = handler.getMethodAnnotation(RequiresUserToken::class.java) ?: return true

            val token = request.getHeader(HEADER_USER_TOKEN) ?: throw InvalidTokenException()
            val entryResult = validateTokenUseCase.execute(token)

            return if (requiresUserToken.allowedStatus.isEmpty() || entryResult.status in requiresUserToken.allowedStatus) {
                request.setAttribute(ATTR_QUEUE_ENTRY, entryResult)
                true
            } else {
                throw TokenNotProcessableException()
            }
        }
        return true
    }

    companion object {
        const val ATTR_QUEUE_ENTRY = "queue_entry"
        private const val HEADER_USER_TOKEN = "User-Token"
    }
}
