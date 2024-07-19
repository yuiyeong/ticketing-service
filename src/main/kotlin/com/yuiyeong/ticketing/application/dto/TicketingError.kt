package com.yuiyeong.ticketing.application.dto

data class TicketingError(
    val statusCode: ErrorStatusCode,
    val code: String,
    val message: String,
)

enum class ErrorStatusCode(
    val value: Int,
) {
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_ERROR(500),
}
