package com.yuiyeong.ticketing.application.dto

import com.yuiyeong.ticketing.application.service.TicketingErrorCode

data class TicketingErrorDto(
    val code: TicketingErrorCode,
    val message: String,
)
