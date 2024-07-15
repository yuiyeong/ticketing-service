package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.application.service.TicketingErrorCode

data class TicketingError(
    val code: TicketingErrorCode,
    val message: String,
)
