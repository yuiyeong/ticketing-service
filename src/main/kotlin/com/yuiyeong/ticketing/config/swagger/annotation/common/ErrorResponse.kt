package com.yuiyeong.ticketing.config.swagger.annotation.common

import com.yuiyeong.ticketing.config.swagger.schema.response.ErrorResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "500",
    description = "서버 오류 발생",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
        ),
    ],
)
annotation class InternalServerErrorResponse

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "401",
    description = "인증 오류 발생",
    content = [
        Content(mediaType = "application/json"),
    ],
)
annotation class NoAuthErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "유효하지 않은 토큰",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "invalid_token",
                            "message": "유효하지 않은 token 입니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class InvalidTokenErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "콘서트를 찾을 수 없음",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "not_found_concert",
                            "message": "요청한 콘서트를 찾을 수 없습니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class NotFoundConcertErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "콘서트 이벤트를 찾을 수 없음",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "not_found_concert_event",
                            "message": "요청한 콘서트 이벤트를 찾을 수 없습니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class NotFoundConcertEventErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "유효하지 않은 좌석",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "invalid_seat",
                            "message": "이미 점유되었거나 예약된 좌석입니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class InvalidSeatErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "좌석을 찾을 수 없음",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "not_found_seat",
                            "message": "콘서트 이벤트에서 해당 좌석을 찾을 수 없습니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class NotFoundSeatErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "잔액 부족",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "insufficient_balance",
                            "message": "잔액이 부족합니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class InsufficientBalanceErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "점유 시간 만료",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "occupation_expired",
                            "message": "좌석 점유 시간이 만료되었습니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class OccupationExpiredErrorResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "유효하지 않은 충전 금액",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "invalid_amount",
                            "message": "유효하지 않은 충전 금액입니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
annotation class InvalidAmountErrorResponse
