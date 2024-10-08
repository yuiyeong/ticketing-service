package com.yuiyeong.ticketing.domain.exception

enum class ErrorCode(
    val message: String,
) {
    INTERNAL_SERVER_ERROR("예기치 못한 에러가 발생했습니다."),
    INVALID_TOKEN("유효하지 않은 token 입니다."),
    TOKEN_NOT_PROCESSABLE("이 token 은 작업할 수 없는 상태압니다."),
    QUEUE_NOT_AVAILABLE("대기열에 참여할 수 없습니다."),
    CONCERT_EVENT_NOT_FOUND("요청한 콘서트 이벤트를 찾을 수 없습니다."),
    WALLET_NOT_FOUND("요청한 사용자의 잔액을 찾을 수 없습니다."),
    WALLET_UNAVAILABLE("요청한 사용자의 지갑을 사용할 수 없습니다."),
    SEAT_UNAVAILABLE("다른 사용자가 선택한 좌석이거나 이미 예약된 좌석입니다."),
    SEAT_ALREADY_UNAVAILABLE("이미 사용불가한 좌석입니다."),
    INVALID_AMOUNT("유효하지 않은 충전 금액입니다."),
    INSUFFICIENT_BALANCE("잔액이 부족합니다."),
    OCCUPATION_NOT_FOUND("선택한 좌석을 찾을 수 없습니다."),
    OCCUPATION_ALREADY_EXPIRED("좌석 선택이 만료되었습니다."),
    OCCUPATION_ALREADY_RELEASED("이미 예약된 좌석입니다."),
    OCCUPATION_INVALID("잘못된 좌석입니다."),
    RESERVATION_NOT_FOUND("요청한 예약을 찾을 수 없습니다."),
    RESERVATION_NOT_OPENED("예약 기간이 아닙니다."),
    RESERVATION_ALREADY_CONFIRMED("이미 예약이 완료되었습니다."),
    TRANSACTION_NOT_FOUND("요청한 트랜잭션을 찾을 수 없습니다."),
}
