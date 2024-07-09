package com.yuiyeong.ticketing.presentation.dto.response

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
