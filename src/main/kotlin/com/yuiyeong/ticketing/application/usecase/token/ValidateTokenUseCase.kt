package com.yuiyeong.ticketing.application.usecase.token

interface ValidateTokenUseCase {
    fun execute(token: String): Long
}
