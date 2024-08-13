package com.yuiyeong.ticketing.interfaces.presentation.dto

class TicketingResponse<T>(
    val data: T,
)

class TicketingListResponse<T>(
    val list: List<T>,
)

class TicketingErrorData(
    val code: String,
    val message: String,
)

class TicketingErrorResponse(
    val error: TicketingErrorData,
)
