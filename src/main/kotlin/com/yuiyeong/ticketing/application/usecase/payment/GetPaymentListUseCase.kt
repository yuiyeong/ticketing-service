package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.PaymentResult

interface GetPaymentListUseCase {
    fun execute(userId: Long): List<PaymentResult>
}
