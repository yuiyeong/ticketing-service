package com.yuiyeong.ticketing.config.swagger.annotation.api

import com.yuiyeong.ticketing.config.swagger.annotation.common.InternalServerErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NoAuthErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.UserTokenHeader
import com.yuiyeong.ticketing.config.swagger.schema.ErrorResponse
import com.yuiyeong.ticketing.config.swagger.schema.QueueStatusResponse
import com.yuiyeong.ticketing.config.swagger.schema.QueueTokenIssuanceRequest
import com.yuiyeong.ticketing.config.swagger.schema.QueueTokenIssuanceResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "대기열 Token 발급", description = "사용자를 대기열에 넣고, 그 대기 정보에 대한 token 을 내려줍니다.")
@RequestBody(
    description = "토큰 발급 요청",
    required = true,
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = QueueTokenIssuanceRequest::class),
        ),
    ],
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = QueueTokenIssuanceResponse::class),
        ),
    ],
)
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class QueueTokenIssuanceApiDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "대기열 정보 조회", description = "발급받은 Token으로 대기 정보를 조회합니다.")
@UserTokenHeader
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = QueueStatusResponse::class),
        ),
    ],
)
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
@ApiResponse(
    responseCode = "404",
    description = "대기열에 없는 토큰",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    value = """
                    {
                        "error": {
                            "code": "not_found_in_queue",
                            "message": "해당 토큰으로 대기 중인 정보를 찾을 수 없습니다."
                        }
                    }
                    """,
                ),
            ],
        ),
    ],
)
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class QueueStatusApiDoc
