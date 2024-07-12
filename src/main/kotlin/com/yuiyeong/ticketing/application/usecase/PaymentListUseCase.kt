package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.PaymentDto

interface PaymentListUseCase {
    fun getHistory(userId: Long): List<PaymentDto>
}
