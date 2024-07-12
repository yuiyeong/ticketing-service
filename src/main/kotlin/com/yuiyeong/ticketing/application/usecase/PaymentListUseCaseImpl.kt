package com.yuiyeong.ticketing.application.usecase

import com.yuiyeong.ticketing.application.dto.PaymentDto
import com.yuiyeong.ticketing.domain.service.PaymentService

class PaymentListUseCaseImpl(
    private val paymentService: PaymentService,
) : PaymentListUseCase {
    override fun getHistory(userId: Long): List<PaymentDto> = paymentService.getHistory(userId).map { PaymentDto.from(it) }
}
