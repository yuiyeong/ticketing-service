package com.yuiyeong.ticketing.application.service

import com.yuiyeong.ticketing.application.dto.TicketingErrorDto
import com.yuiyeong.ticketing.domain.exception.InsufficientBalanceException
import com.yuiyeong.ticketing.domain.exception.InvalidAmountException
import com.yuiyeong.ticketing.domain.exception.InvalidSeatStatusException
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.InvalidTokenStatusException
import com.yuiyeong.ticketing.domain.exception.NotFoundConcertEventException
import com.yuiyeong.ticketing.domain.exception.NotFoundConcertException
import com.yuiyeong.ticketing.domain.exception.NotFoundSeatException
import com.yuiyeong.ticketing.domain.exception.NotFoundTokenException
import com.yuiyeong.ticketing.domain.exception.NotFoundWalletException
import com.yuiyeong.ticketing.domain.exception.OccupationExpiredException
import org.springframework.stereotype.Service

@Service
class TicketingExceptionService {
    fun handleException(ex: Exception): TicketingErrorDto =
        when (ex) {
            is InvalidTokenException ->
                TicketingErrorDto(TicketingErrorCode.INVALID_TOKEN, ex.notNullMessage)

            is InvalidTokenStatusException ->
                TicketingErrorDto(TicketingErrorCode.INVALID_TOKEN_STATUS, ex.notNullMessage)

            is InvalidAmountException ->
                TicketingErrorDto(TicketingErrorCode.INVALID_AMOUNT, ex.notNullMessage)

            is InvalidSeatStatusException ->
                TicketingErrorDto(TicketingErrorCode.INVALID_SEAT_STATUS, ex.notNullMessage)

            is InsufficientBalanceException ->
                TicketingErrorDto(TicketingErrorCode.INSUFFICIENT_BALANCE, ex.notNullMessage)

            is OccupationExpiredException ->
                TicketingErrorDto(TicketingErrorCode.OCCUPATION_EXPIRED, ex.notNullMessage)

            is NotFoundTokenException ->
                TicketingErrorDto(TicketingErrorCode.NOT_FOUND_IN_QUEUE, ex.notNullMessage)

            is NotFoundConcertException ->
                TicketingErrorDto(TicketingErrorCode.NOT_FOUND_CONCERT, ex.notNullMessage)

            is NotFoundConcertEventException ->
                TicketingErrorDto(TicketingErrorCode.NOT_FOUND_CONCERT_EVENT, ex.notNullMessage)

            is NotFoundSeatException ->
                TicketingErrorDto(TicketingErrorCode.NOT_FOUND_SEAT, ex.notNullMessage)

            is NotFoundWalletException ->
                TicketingErrorDto(TicketingErrorCode.NOT_FOUND_WALLET, ex.notNullMessage)

            else -> TicketingErrorDto(TicketingErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.")
        }
}

enum class TicketingErrorCode {
    INVALID_TOKEN,
    INVALID_TOKEN_STATUS,
    INVALID_SEAT_STATUS,
    INSUFFICIENT_BALANCE,
    OCCUPATION_EXPIRED,
    INVALID_AMOUNT,
    NOT_FOUND_IN_QUEUE,
    NOT_FOUND_CONCERT,
    NOT_FOUND_CONCERT_EVENT,
    NOT_FOUND_SEAT,
    NOT_FOUND_WALLET,
    INTERNAL_SERVER_ERROR,
    ;

    override fun toString(): String = name.lowercase()
}
