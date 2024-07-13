package com.yuiyeong.ticketing.application.service

import com.yuiyeong.ticketing.application.dto.TicketingError
import com.yuiyeong.ticketing.domain.exception.ConcertEventNotFoundException
import com.yuiyeong.ticketing.domain.exception.ConcertNotFoundException
import com.yuiyeong.ticketing.domain.exception.InsufficientBalanceException
import com.yuiyeong.ticketing.domain.exception.InvalidChargeAmountException
import com.yuiyeong.ticketing.domain.exception.InvalidTokenException
import com.yuiyeong.ticketing.domain.exception.OccupationExpiredException
import com.yuiyeong.ticketing.domain.exception.SeatNotFoundException
import com.yuiyeong.ticketing.domain.exception.SeatUnavailableException
import com.yuiyeong.ticketing.domain.exception.TokenNotFoundException
import com.yuiyeong.ticketing.domain.exception.TokenNotProcessableException
import com.yuiyeong.ticketing.domain.exception.WalletNotFoundException
import org.springframework.stereotype.Service

@Service
class TicketingExceptionService {
    fun handleException(ex: Exception): TicketingError =
        when (ex) {
            is InvalidTokenException ->
                TicketingError(TicketingErrorCode.INVALID_TOKEN, ex.notNullMessage)

            is TokenNotProcessableException ->
                TicketingError(TicketingErrorCode.INVALID_TOKEN_STATUS, ex.notNullMessage)

            is InvalidChargeAmountException ->
                TicketingError(TicketingErrorCode.INVALID_AMOUNT, ex.notNullMessage)

            is SeatUnavailableException ->
                TicketingError(TicketingErrorCode.INVALID_SEAT_STATUS, ex.notNullMessage)

            is InsufficientBalanceException ->
                TicketingError(TicketingErrorCode.INSUFFICIENT_BALANCE, ex.notNullMessage)

            is OccupationExpiredException ->
                TicketingError(TicketingErrorCode.OCCUPATION_EXPIRED, ex.notNullMessage)

            is TokenNotFoundException ->
                TicketingError(TicketingErrorCode.NOT_FOUND_IN_QUEUE, ex.notNullMessage)

            is ConcertNotFoundException ->
                TicketingError(TicketingErrorCode.NOT_FOUND_CONCERT, ex.notNullMessage)

            is ConcertEventNotFoundException ->
                TicketingError(TicketingErrorCode.NOT_FOUND_CONCERT_EVENT, ex.notNullMessage)

            is SeatNotFoundException ->
                TicketingError(TicketingErrorCode.NOT_FOUND_SEAT, ex.notNullMessage)

            is WalletNotFoundException ->
                TicketingError(TicketingErrorCode.NOT_FOUND_WALLET, ex.notNullMessage)

            else -> TicketingError(TicketingErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.")
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
