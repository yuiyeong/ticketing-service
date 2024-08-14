package com.yuiyeong.ticketing.interfaces.api.interceptor

import com.yuiyeong.ticketing.application.annotation.RequiresUserToken
import com.yuiyeong.ticketing.application.usecase.queue.CheckActiveTokenUseCase
import com.yuiyeong.ticketing.application.usecase.token.ValidateTokenUseCase
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class UserTokenInterceptor(
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val checkActiveTokenUseCase: CheckActiveTokenUseCase,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod) {
            val requiresUserToken = handler.getMethodAnnotation(RequiresUserToken::class.java) ?: return true

            val token = request.getHeader(HEADER_USER_TOKEN) ?: throw InvalidTokenException()
            if (requiresUserToken.onlyActive) {
                checkActiveTokenUseCase.execute(token)
            }

            val userId = validateTokenUseCase.execute(token)
            request.setAttribute(ATTR_USER_ID, userId)
            request.setAttribute(ATTR_TOKEN, token)
        }
        return true
    }

    companion object {
        const val ATTR_USER_ID = "user_id"
        const val ATTR_TOKEN = "token"
        private const val HEADER_USER_TOKEN = "User-Token"
    }
}
