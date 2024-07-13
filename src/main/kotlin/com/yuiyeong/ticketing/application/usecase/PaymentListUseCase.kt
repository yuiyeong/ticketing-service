package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.PaymentResult

interface PaymentListUseCase {
    fun getHistory(userId: Long): List<PaymentResult>
}
