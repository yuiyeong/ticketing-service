package com.yuiyeong.ticketing.domain.exception

sealed class TicketingException(
    message: String,
) : RuntimeException(message) {
    val notNullMessage: String
        get() = message!!
}

class InvalidTokenException : TicketingException("유효하지 않은 token 입니다.")

class InvalidTokenStatusException : TicketingException("이 token 은 작업할 수 없는 상태압니다.")

class NotFoundTokenException : TicketingException("해당 토큰으로 대기 중인 정보를 찾을 수 없습니다.")

class NotFoundConcertException : TicketingException("요청한 콘서트를 찾을 수 없습니다.")

class NotFoundConcertEventException : TicketingException("요청한 콘서트 이벤트를 찾을 수 없습니다.")

class NotFoundSeatException : TicketingException("요청한 좌석을 찾을 수 없습니다.")

class NotFoundWalletException : TicketingException("요청한 사용자의 잔액을 찾을 수 없습니다.")

class InvalidSeatStatusException : TicketingException("다른 사용자가 선택한 좌석이거나 이미 예약된 좌석입니다.")

class InsufficientBalanceException : TicketingException("잔액이 부족합니다.")

class OccupationExpiredException : TicketingException("좌석 선택이 만료되었습니다.")

class InvalidAmountException : TicketingException("유효하지 않은 충전 금액입니다.")

class OutOfPeriodException : TicketingException("예약 기간이 아닙니다.")
