package com.yuiyeong.ticketing.presentation.advice

import com.yuiyeong.ticketing.application.dto.ErrorStatusCode
import com.yuiyeong.ticketing.application.service.TicketingExceptionService
import com.yuiyeong.ticketing.presentation.dto.TicketingErrorData
import com.yuiyeong.ticketing.presentation.dto.TicketingErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ApiControllerAdvice(
    private val exceptionService: TicketingExceptionService,
) : ResponseEntityExceptionHandler() {
    private val adviceLogger = LoggerFactory.getLogger(ApiControllerAdvice::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<TicketingErrorResponse> {
        val error = exceptionService.handleException(e)
        val status =
            when (error.statusCode) {
                ErrorStatusCode.BAD_REQUEST -> {
                    adviceLogger.warn("${error.code};${error.message}")
                    HttpStatus.BAD_REQUEST
                }

                ErrorStatusCode.NOT_FOUND -> {
                    adviceLogger.warn("${error.code};${error.message}")
                    HttpStatus.NOT_FOUND
                }

                ErrorStatusCode.INTERNAL_ERROR -> {
                    adviceLogger.error(error.code, e)
                    HttpStatus.INTERNAL_SERVER_ERROR
                }
            }
        return ResponseEntity(
            TicketingErrorResponse(TicketingErrorData(error.code, error.message)),
            status,
        )
    }
}
