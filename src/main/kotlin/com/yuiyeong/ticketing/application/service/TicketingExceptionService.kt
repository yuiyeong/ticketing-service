package com.yuiyeong.ticketing.application.service

import com.yuiyeong.ticketing.application.dto.ErrorStatusCode
import com.yuiyeong.ticketing.application.dto.TicketingError
import com.yuiyeong.ticketing.domain.exception.BadRequestException
import com.yuiyeong.ticketing.domain.exception.ErrorCode
import com.yuiyeong.ticketing.domain.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class TicketingExceptionService {
    fun handleException(e: Exception): TicketingError =
        when (e) {
            is BadRequestException ->
                TicketingError(
                    ErrorStatusCode.BAD_REQUEST,
                    e.errorCode.name.lowercase(),
                    e.errorCode.message,
                )
            is NotFoundException ->
                TicketingError(
                    ErrorStatusCode.NOT_FOUND,
                    e.errorCode.name.lowercase(),
                    e.errorCode.message,
                )
            else ->
                TicketingError(
                    ErrorStatusCode.INTERNAL_ERROR,
                    ErrorCode.INTERNAL_SERVER_ERROR.name.lowercase(),
                    ErrorCode.INTERNAL_SERVER_ERROR.message,
                )
        }
}
