package com.yuiyeong.ticketing.config.swagger.annotation.api

import com.yuiyeong.ticketing.config.swagger.annotation.common.InsufficientBalanceErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.InternalServerErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.InvalidSeatErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.InvalidTokenErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NoAuthErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NotFoundConcertErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NotFoundConcertEventErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NotFoundSeatErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.OccupationExpiredErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.UserTokenHeader
import com.yuiyeong.ticketing.config.swagger.schema.request.OccupySeatRequest
import com.yuiyeong.ticketing.config.swagger.schema.request.ReserveSeatRequest
import com.yuiyeong.ticketing.config.swagger.schema.response.AvailableConcertEventsResponse
import com.yuiyeong.ticketing.config.swagger.schema.response.AvailableSeatsResponse
import com.yuiyeong.ticketing.config.swagger.schema.response.ConcertResponse
import com.yuiyeong.ticketing.config.swagger.schema.response.OccupySeatResponse
import com.yuiyeong.ticketing.config.swagger.schema.response.ReserveSeatResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "콘서트 목록 조회", description = "모든 콘서트의 목록을 조회합니다.")
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ConcertResponse::class),
        ),
    ],
)
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class ConcertsApiDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "예약 가능한 콘서트 이벤트 조회", description = "특정 콘서트의 예약 가능한 날짜 목록을 조회합니다.")
@UserTokenHeader
@Parameter(
    name = "concertId",
    description = "조회할 콘서트의 ID",
    required = true,
    `in` = ParameterIn.PATH,
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = AvailableConcertEventsResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "list": [
                            {
                                "id": 1,
                                "date": "2023-07-01"
                            },
                            {
                                "id": 2,
                                "date": "2023-07-02"
                            }
                        ]
                    }
                    """,
                ),
            ],
        ),
    ],
)
@InvalidTokenErrorResponse
@NotFoundConcertErrorResponse
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class AvailableConcertEventsApiDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "예약 가능한 좌석 조회", description = "특정 콘서트 이벤트의 예약 가능한 좌석 목록을 조회합니다.")
@UserTokenHeader
@Parameter(
    name = "concertEventId",
    description = "조회할 콘서트 이벤트의 ID",
    required = true,
    `in` = ParameterIn.PATH,
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = AvailableSeatsResponse::class),
        ),
    ],
)
@InvalidTokenErrorResponse
@NotFoundConcertEventErrorResponse
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class AvailableSeatsApiDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "좌석 점유", description = "특정 콘서트 이벤트의 좌석을 점유합니다.")
@UserTokenHeader
@Parameter(
    name = "concertEventId",
    description = "좌석을 점유할 콘서트 이벤트의 ID",
    required = true,
    `in` = ParameterIn.PATH,
)
@RequestBody(
    required = true,
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = OccupySeatRequest::class),
        ),
    ],
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = OccupySeatResponse::class),
        ),
    ],
)
@InvalidTokenErrorResponse
@NotFoundConcertEventErrorResponse
@InvalidSeatErrorResponse
@NotFoundSeatErrorResponse
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class OccupySeatApiDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "좌석 예약", description = "점유한 좌석에 대해 결제 및 예약을 진행합니다.")
@UserTokenHeader
@Parameter(
    name = "concertEventId",
    description = "좌석을 예약할 콘서트 이벤트의 ID",
    required = true,
    `in` = ParameterIn.PATH,
)
@RequestBody(
    required = true,
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReserveSeatRequest::class),
        ),
    ],
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReserveSeatResponse::class),
        ),
    ],
)
@InvalidTokenErrorResponse
@NotFoundConcertEventErrorResponse
@InvalidSeatErrorResponse
@NotFoundSeatErrorResponse
@InsufficientBalanceErrorResponse
@OccupationExpiredErrorResponse
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class ReserveSeatApiDoc
