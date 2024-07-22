package com.yuiyeong.ticketing.application.usecase.payment

import com.yuiyeong.ticketing.application.dto.payment.PaymentResult

interface GetPaymentListUseCase {
    fun execute(userId: Long): List<PaymentResult>
}
