package com.yuiyeong.ticketing.config.swagger.annotation.api

import com.yuiyeong.ticketing.config.swagger.annotation.common.InternalServerErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.InvalidAmountErrorResponse
import com.yuiyeong.ticketing.config.swagger.annotation.common.NoAuthErrorResponse
import com.yuiyeong.ticketing.config.swagger.schema.request.ChargeWalletRequest
import com.yuiyeong.ticketing.config.swagger.schema.response.ChargeWalletResponse
import com.yuiyeong.ticketing.config.swagger.schema.response.WalletBalanceResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "잔액 조회", description = "사용자의 현재 잔액 정보를 조회합니다.")
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
            schema = Schema(implementation = WalletBalanceResponse::class),
        ),
    ],
)
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class WalletBalanceApiDoc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(summary = "잔액 충전", description = "사용자의 잔액을 충전합니다.")
@Parameter(
    name = "userId",
    description = "충전할 사용자의 ID",
    required = true,
    `in` = ParameterIn.PATH,
)
@RequestBody(
    required = true,
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ChargeWalletRequest::class),
        ),
    ],
)
@ApiResponse(
    responseCode = "200",
    description = "성공",
    content = [
        Content(
            mediaType = "application/json",
            schema = Schema(implementation = ChargeWalletResponse::class),
        ),
    ],
)
@InvalidAmountErrorResponse
@NoAuthErrorResponse
@InternalServerErrorResponse
annotation class ChargeWalletApiDoc
