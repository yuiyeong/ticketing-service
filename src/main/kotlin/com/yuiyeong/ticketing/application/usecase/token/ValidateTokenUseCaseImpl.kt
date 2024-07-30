package com.yuiyeong.ticketing.application.usecase.token

import com.yuiyeong.ticketing.domain.service.queue.TokenService
import org.springframework.stereotype.Component

@Component
class ValidateTokenUseCaseImpl(
    private val tokenService: TokenService,
) : ValidateTokenUseCase {
    override fun execute(token: String): Long = tokenService.validateToken(token)
}
