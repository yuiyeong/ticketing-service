package com.yuiyeong.ticketing.application.usecase.queue

interface CheckActiveTokenUseCase {
    fun execute(token: String)
}
