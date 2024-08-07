package com.yuiyeong.ticketing.config.swagger.annotation.api

import com.yuiyeong.ticketing.config.swagger.annotation.common.InternalServerErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NoAuthErrorResponse
import com.yuiyeong.ticketing.config.swagger.schema.request.PayRequest
import com.yuiyeong.ticketing.config.swagger.schema.response.PaymentHistoryResponse
import com.yuiyeong.ticketing.config.swagger.schema.response.PaymentResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "결제", description = "사용자가 한 예약에 대한 결제를 합니다.")
@RequestBody(
    required = true,
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = PayRequest::class),
        ),
    ],
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = PaymentResponse::class),
        ),
    ],
)
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class PayApiDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "결제 내역 목록 조회", description = "사용자의 결제 내역 목록을 조회합니다.")
@Parameter(
    name = "userId",
    description = "조회할 사용자의 ID",
    required = true,
    `in` = ParameterIn.PATH,
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = PaymentHistoryResponse::class),
        ),
    ],
)
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class PaymentHistoryApiDoc
