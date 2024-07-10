package com.yuiyeong.ticketing.config.swagger.annotation.api

import com.yuiyeong.ticketing.config.swagger.annotation.common.InternalServerErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.InvalidTokenErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NoAuthErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NotFoundConcertErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.UserTokenHeader
import com.yuiyeong.ticketing.config.swagger.schema.AvailableConcertEventsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

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
