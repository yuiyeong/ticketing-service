package com.yuiyeong.ticketing.presentation.advice

import com.yuiyeong.ticketing.application.service.TicketingErrorCode
import com.yuiyeong.ticketing.application.service.TicketingExceptionService
import com.yuiyeong.ticketing.presentation.dto.response.TicketingErrorData
import com.yuiyeong.ticketing.presentation.dto.response.TicketingErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ApiControllerAdvice(
    private val exceptionService: TicketingExceptionService,
) : ResponseEntityExceptionHandler() {
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<TicketingErrorResponse> {
        val errorDto = exceptionService.handleException(e)

        if (errorDto.code == TicketingErrorCode.INTERNAL_SERVER_ERROR) {
            logger.error(e)
        } else {
            logger.info("Error: ${errorDto.code} | ${errorDto.message}")
        }

        return ResponseEntity(
            TicketingErrorResponse(TicketingErrorData(errorDto.code.toString(), errorDto.message)),
            mapErrorCodeToHttpStatus(errorDto.code),
        )
    }

    private fun mapErrorCodeToHttpStatus(errorCode: TicketingErrorCode): HttpStatus =
        when (errorCode) {
            TicketingErrorCode.INVALID_TOKEN,
            TicketingErrorCode.INVALID_SEAT_STATUS,
            TicketingErrorCode.INSUFFICIENT_BALANCE,
            TicketingErrorCode.OCCUPATION_EXPIRED,
            TicketingErrorCode.INVALID_AMOUNT,
            -> HttpStatus.BAD_REQUEST

            TicketingErrorCode.NOT_FOUND_IN_QUEUE,
            TicketingErrorCode.NOT_FOUND_CONCERT,
            TicketingErrorCode.NOT_FOUND_CONCERT_EVENT,
            TicketingErrorCode.NOT_FOUND_SEAT,
            TicketingErrorCode.NOT_FOUND_WALLET,
            -> HttpStatus.NOT_FOUND

            TicketingErrorCode.INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
        }
}
