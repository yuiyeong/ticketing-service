package com.yuiyeong.ticketing.domain.exception

sealed class TicketingException(
    message: String,
) : RuntimeException(message) {
    val notNullMessage: String
        get() = message!!
}

class InvalidTokenException : TicketingException("유효하지 않은 token 입니다.")

class TokenNotProcessableException : TicketingException("이 token 은 작업할 수 없는 상태압니다.")

class TokenNotFoundException : TicketingException("해당 토큰으로 대기 중인 정보를 찾을 수 없습니다.")

class ConcertNotFoundException : TicketingException("요청한 콘서트를 찾을 수 없습니다.")

class ConcertEventNotFoundException : TicketingException("요청한 콘서트 이벤트를 찾을 수 없습니다.")

class SeatNotFoundException : TicketingException("요청한 좌석을 찾을 수 없습니다.")

class WalletNotFoundException : TicketingException("요청한 사용자의 잔액을 찾을 수 없습니다.")

class SeatUnavailableException : TicketingException("다른 사용자가 선택한 좌석이거나 이미 예약된 좌석입니다.")

class SeatAlreadyUnavailableException : TicketingException("이미 사용불가한 좌석입니다.")

class InvalidAmountException : TicketingException("유효하지 않은 충전 금액입니다.")

class InsufficientBalanceException : TicketingException("잔액이 부족합니다.")

class OccupationNotFoundException : TicketingException("선택한 좌석을 찾을 수 없습니다.")

class OccupationAlreadyExpiredException : TicketingException("좌석 선택이 만료되었습니다.")

class OccupationAlreadyReleaseException : TicketingException("이미 예약된 좌석입니다.")

class ReservationNotFoundException : TicketingException("요청한 예약을 찾을 수 없습니다.")

class ReservationNotOpenedException : TicketingException("예약 기간이 아닙니다.")

class ReservationAlreadyConfirmedException : TicketingException("이미 예약이 완료되었습니다.")

class ReservationAlreadyCanceledException : TicketingException("이미 취소된 예약입니다.")

class QueueEntryAlreadyProcessingException : TicketingException("이미 작업 중인 상태입니다.")

class QueueEntryAlreadyExitedException : TicketingException("이미 대기열에서 나간 상태입니다.")

class QueueEntryAlreadyExpiredException : TicketingException("이미 만료된 token 입니다.")

class QueueEntryOverdueException : TicketingException("만료 시간이 지난 token 입니다.")

class TransactionNotFoundException : TicketingException("요청한 트랜잭션을 찾을 수 없습니다.")
