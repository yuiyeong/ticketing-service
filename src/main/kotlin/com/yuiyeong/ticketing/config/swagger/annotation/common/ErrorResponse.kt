package com.yuiyeong.ticketing.config.swagger.annotation.common

import com.yuiyeong.ticketing.config.swagger.schema.ErrorResponse
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
