package com.yuiyeong.ticketing.domain.exception

sealed class TicketingException(
    val errorCode: ErrorCode,
) : RuntimeException()

sealed class BadRequestException(
    errorCode: ErrorCode,
) : TicketingException(errorCode)

sealed class NotFoundException(
    errorCode: ErrorCode,
) : TicketingException(errorCode)

class InvalidTokenException : BadRequestException(ErrorCode.INVALID_TOKEN)

class TokenNotProcessableException : BadRequestException(ErrorCode.TOKEN_NOT_PROCESSABLE)

class TokenNotFoundException : NotFoundException(ErrorCode.TOKEN_NOT_FOUND)

class ConcertNotFoundException : NotFoundException(ErrorCode.CONCERT_NOT_FOUND)

class ConcertEventNotFoundException : NotFoundException(ErrorCode.CONCERT_EVENT_NOT_FOUND)

class SeatNotFoundException : NotFoundException(ErrorCode.SEAT_NOT_FOUND)

class WalletNotFoundException : NotFoundException(ErrorCode.WALLET_NOT_FOUND)

class SeatUnavailableException : BadRequestException(ErrorCode.SEAT_UNAVAILABLE)

class SeatAlreadyUnavailableException : BadRequestException(ErrorCode.SEAT_ALREADY_UNAVAILABLE)

class InvalidAmountException : BadRequestException(ErrorCode.INVALID_AMOUNT)

class InsufficientBalanceException : BadRequestException(ErrorCode.INSUFFICIENT_BALANCE)

class OccupationNotFoundException : NotFoundException(ErrorCode.OCCUPATION_NOT_FOUND)

class OccupationAlreadyExpiredException : BadRequestException(ErrorCode.OCCUPATION_ALREADY_EXPIRED)

class OccupationAlreadyReleaseException : BadRequestException(ErrorCode.OCCUPATION_ALREADY_RELEASED)

class OccupationInvalidException : BadRequestException(ErrorCode.OCCUPATION_INVALID)

class ReservationNotFoundException : NotFoundException(ErrorCode.RESERVATION_NOT_FOUND)

class ReservationNotOpenedException : BadRequestException(ErrorCode.RESERVATION_NOT_OPENED)

class ReservationAlreadyConfirmedException : BadRequestException(ErrorCode.RESERVATION_ALREADY_CONFIRMED)

class QueueEntryAlreadyProcessingException : BadRequestException(ErrorCode.QUEUE_ENTRY_ALREADY_PROCESSING)

class QueueEntryAlreadyExitedException : BadRequestException(ErrorCode.QUEUE_ENTRY_ALREADY_EXITED)

class QueueEntryAlreadyExpiredException : BadRequestException(ErrorCode.QUEUE_ENTRY_ALREADY_EXPIRED)

class QueueEntryOverdueException : BadRequestException(ErrorCode.QUEUE_ENTRY_OVERDUE)

class TransactionNotFoundException : NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND)
