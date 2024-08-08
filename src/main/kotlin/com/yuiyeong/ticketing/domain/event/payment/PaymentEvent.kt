package com.yuiyeong.ticketing.domain.event.payment

import com.yuiyeong.ticketing.domain.model.wallet.Transaction

data class PaymentEvent(
    val userId: Long,
    val reservationId: Long,
    val transaction: Transaction?,
    val failureReason: String?,
)
